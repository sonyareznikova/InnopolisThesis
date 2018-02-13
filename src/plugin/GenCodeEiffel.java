package plugin;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eventb.core.EventBPlugin;
import org.eventb.core.ISCAction;
import org.eventb.core.ISCAxiom;
import org.eventb.core.ISCCarrierSet;
import org.eventb.core.ISCConstant;
import org.eventb.core.ISCEvent;
import org.eventb.core.ISCGuard;
import org.eventb.core.ISCInternalContext;
import org.eventb.core.ISCInvariant;
import org.eventb.core.ISCParameter;
import org.eventb.core.ISCVariable;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.basis.SCMachineRoot;
import org.eventb.ui.eventbeditor.IEventBEditor;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

import rodinDB.RodinDBElements;


public class GenCodeEiffel implements IEditorActionDelegate{

	IEventBEditor<?> editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof IEventBEditor)
			editor = (IEventBEditor<?>) targetEditor;
	}

	// Implementation of the translation rules (Eiffel code Generation)
	Translator oper;
	
	//Implementation of translation into Eiffel files
	EiffelTranslator eifTran;

	// Implementation of the translation for Specification Drivers (Eiffel)
	SpecDriver spec;
	
	// access to the Rodin's DataBase
	RodinDBElements rodinDB;

	public void run(IAction action){
		
		rodinDB = new RodinDBElements();
		
		final IRodinFile rodinFile = getSelectedComponent();

		String projectName = rodinFile.getParent().getElementName();

		String machineName = rodinFile.getBareName().toString();

		oper = new Translator();
		
		eifTran = new EiffelTranslator();
		

		String output;
		try {
			output = test(rodinFile,machineName, projectName);
			//FileWriter writer = new FileWriter("file.txt");
			//writer.write(output);
			//writer.close();
			System.out.println(output);
			MessageDialog.openInformation(
					PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getShell(),
					"Success",
					"Successful Translation.");
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Returns the generated Eiffel code from machine 'machineName'
	//TODO: implement
	public String test(IRodinFile rodinFile, String machineName, String packageName) throws RodinDBException, CoreException, IOException {
		String result = "class " + machineName + "\n";
		result += "create initialisation";
		ISCInternalContext[] ctxs = rodinDB.getMachineContexts(rodinFile, machineName);
		//there is no need for parameter types but the method is one for contexts and event guards
		HashMap<String,String> emptyTypes = new HashMap<String,String>();
		ArrayList<String> contexts = getContexts(ctxs, emptyTypes);
		for (String c: contexts) {
			result += c + "\n";
		}
		
		//getting all events
		ISCEvent[] evts = rodinDB.getMachineEvents(rodinFile, machineName); // info from Rodin's DB
		
		// translate initialisation
		System.out.println("init");
		ArrayList<String> InitActions = InitialisationDoTranslation(evts[0]);
		ArrayList<String> InitEnsure = InitialisationEnsureTranslation(evts[0]);
		
		result += "\n" + "feature -- Initialisation\r\n" + 
				"	initialisation\r\n" + 
				"		do\r\n";
		for (String e: InitActions) result += "			"+ e + "\n";
		
		result += "\n		ensure\r\n";
		
		for (String a: InitEnsure) result += "			"+ a + "\n"; 
				
		result += "		end";
		
		//start translating other events
		result += "\n" + "feature -- Events";
		for (ISCEvent evt : evts){
			
			if (!evt.getLabel().equals("INITIALISATION")){ //INITIALISATION is a bit different
				result += "\n	"+ evt.getLabel() + " (";
				System.out.println(evt.getLabel());
				//check parameters and create a mapping between variables and types for each event
				//inserting event parameters into the resulting code
				HashMap<String,String> params = EventParameters(evt);
				int counter = 0;
				int len = params.size();
				for (String var: params.keySet()) {
					counter++;
					if (counter != len) result += var + ": " + params.get(var) + "; ";
					else result += var + ": " + params.get(var);
				}
				result += ")";
				
				
				ArrayList<String> Guards = eventGuards(evt, params);
				result += "\n		require\n";
				for (int i=0;i<Guards.size();i++){
					result += "			" + "grd" + Integer.toString(i+1) + ": " + Guards.get(i) + "\n";
				}
				
				result += "		do\n";
				ArrayList<String> Actions = eventActions(evt, params);
				for (String a: Actions) {
					result += "			" + a + "\n";
				}	
			}	
		}
		
		result += "\nfeature -- Access\n";
		//Go through variables - get types
		result +=  machineVariables(rodinFile, machineName);
		
		//translate invariants
		System.out.println("invariants");
		ISCInvariant[] invs = rodinDB.getMachineInvariants(rodinFile, machineName);
		result += "\ninvariant\n";
		for (int i=0;i<invs.length;i++) {
			//using emptyTypes here as well as the method is the same for invariants and event guards
			result += "		inv" + Integer.toString(i+1) + ": " + (translation(invs[i].getPredicateString(), emptyTypes)) + "\n";
		}
		
		result += "\nend";
		return result;
	}
	
	//get POs
	
	//translate initialisation actions for the DO part
	public ArrayList<String> InitialisationDoTranslation(ISCEvent evt) throws CoreException {
		ArrayList<String> res = new ArrayList<String>();
		ISCAction[] evtActions = rodinDB.getEvtActions(evt);
		for (ISCAction act: evtActions) {
			res.add(oper.parseForDoInit(act.getAssignmentString()));
		}
		return res;
	}
	
	//translate initialisation actions for the ENSURE part
	public ArrayList<String> InitialisationEnsureTranslation(ISCEvent evt) throws CoreException {
		ArrayList<String> res = new ArrayList<String>();
		ISCAction[] evtActions =  rodinDB.getEvtActions(evt);
		for (ISCAction act: evtActions) {
			res.add(oper.parseInitialisation(act.getAssignmentString()));
		}
		return res;
	}
	
	public HashMap<String,String> EventParameters(ISCEvent evt) throws RodinDBException {
		ArrayList<String> res = new ArrayList<String>();
		HashMap<String, String> res1 = new HashMap<String, String>();
		ISCParameter[] parameters = evt.getSCParameters();
		for (ISCParameter p : parameters) {
			final FormulaFactory f = FormulaFactory.getDefault();
			try {
				//System.out.println(p.getType(f).toString());
				String name = p.getElementName().toString();
				String type = getType(p.getType(f).toString());
				res.add(name + ": " + type);
				res1.put(name, type);
				//res.add(p.getElementName().toString() + ": " + getType(p.getType(f).toString()));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return res1;
	}
	
	//Returns contexts
	public ArrayList<String> getContexts(ISCInternalContext[] ctxs, HashMap<String,String> types) throws RodinDBException, IOException {
		ArrayList<String> result = new ArrayList<String>();
		for (ISCInternalContext ctx: ctxs) {
			result.add(ctx.getElementName());
			
			ISCCarrierSet[] carriersets = ctx.getSCCarrierSets();
			result.add("SETS");
			for (ISCCarrierSet set: carriersets) {
				String element_name = set.getElementName();
				result.add(element_name);
				eifTran.translateCarrierSets(element_name);
			}
			
			//TODO: translate into a class
			ISCConstant[] constants = ctx.getSCConstants();
			result.add("\nCONSTANTS");
			for (ISCConstant c: constants) {
				result.add(c.getElementName());
			}
			
			ISCAxiom[] axioms = ctx.getSCAxioms();
			result.add("\nAXIOMS");
			for (ISCAxiom ax: axioms) {
				result.add(translation(ax.getPredicateString(), types));
			}	
		}
		return result;
	}
	
	// Returns the translation in Eiffel of the guards of 'machineName'
	public ArrayList<String> eventGuards(ISCEvent event, HashMap<String,String> types) throws RodinDBException {
		ArrayList<String> res = new ArrayList<String>(); 
		ISCGuard[] evtGuards = rodinDB.getEvtGuards (event);
		for (int i=0; i<evtGuards.length;i++){
			res.add (translation(evtGuards[i].getPredicateString(), types));			
		}
		return res;
	}
	
	// Returns the translation of EB variables into Eiffel
	public String machineVariables(IRodinFile rodinFile, String machineName) throws RodinDBException {
		StringBuffer res = new StringBuffer("");
		ISCVariable[] variables = rodinDB.getMachineVariables(rodinFile,machineName);
		for (ISCVariable var : variables){
			res.append(variable(var));
		}
		return res.toString();
	}
	
	//Returns translation of variable (var) type from EB to Eiffel.
		public String variable(ISCVariable var) throws RodinDBException{
			final FormulaFactory f = FormulaFactory.getDefault();
			String res = null;
			try {
				//System.out.println(var.getType(f).toString());
				res = "\n		" + var.getElementName().toString() + ": " + getType(var.getType(f).toString());
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return res;
		}

		public String getType(String varDef){
			return oper.parseExpression(varDef);
		}
		
		public String translateAssignment(String assig, HashMap<String,String> types){
			return oper.Assignment(assig, types);
		}

		// Returns the translation in Eiffel of the actions of 'event'
		public ArrayList<String> eventActions(ISCEvent event, HashMap<String,String> types) throws CoreException {
			ISCAction[] evtActions =  rodinDB.getEvtActions(event);
			ArrayList<String> res = new ArrayList<String>();
			for (ISCAction evtAction : evtActions){
				res.add(translateAssignment(evtAction.getAssignmentString(), types));
			}

			return res;
		}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	private ISelection selection;

	private IRodinFile getSelectedComponent() {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() == 1) {
				return EventBPlugin.asEventBFile(ssel.getFirstElement());
			}
		}
		return null;
	}

	public String translation (String text, HashMap<String,String> types){
		String res = "";
		try {
			ArrayList<String> parsed = oper.parsePredicate(text, types);
			for (String l: parsed) {
				res += l;
			}
		}
		catch (Exception ex)
		{
			res = "(* Error *)";
		}
		return res; 
	}

	private static void displayInStatusBar(String message, boolean error) {
		final IWorkbenchWindow activeWorkbenchWindow = PlatformUI
				.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return;
		final IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
		if (page == null)
			return;
		final IViewPart view = page
				.findView("fr.systerel.explorer.navigator.view");
		if (view == null)
			return;
		final IViewSite site = view.getViewSite();
		final IStatusLineManager slManager = site.getActionBars()
				.getStatusLineManager();
		slManager.setMessage(message);
	}

}