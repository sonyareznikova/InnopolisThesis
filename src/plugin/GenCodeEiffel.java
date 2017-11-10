package plugin;

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

		String output;
		try {
			output = test(rodinFile,machineName, projectName);
			System.out.println(output);
			MessageDialog.openInformation(
					PlatformUI.getWorkbench().
					getActiveWorkbenchWindow().getShell(),
					"Success",
					"Successful Translation.");
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	// Returns the generated Eiffel code from machine 'machineName'
	//TODO: implement
	public String test(IRodinFile rodinFile, String machineName, String packageName) throws RodinDBException, CoreException {
		String result = "";
		ISCInternalContext[] ctxs = rodinDB.getMachineContexts(rodinFile, machineName);
		ArrayList<String> contexts = getContexts(ctxs);
		for (String c: contexts) {
			result += c + "\n";
		}
		
		result += "\n\nVars\n";
		//Go through variables - get types
		result += machineVariables(rodinFile, machineName);
		
		ISCInvariant[] invs = rodinDB.getMachineInvariants(rodinFile, machineName);
		result += "\nInvariants\n";
		for (ISCInvariant inv: invs) {
			result += (translation(inv.getPredicateString())) + "\n";
		}
		
		
		ISCEvent[] evts = rodinDB.getMachineEvents(rodinFile, machineName); // info from Rodin's DB
		// Go through each event
		for (ISCEvent evt : evts){
			result += "\n"+ evt.getLabel();
			//check parameters
			ISCParameter[] parameters = evt.getSCParameters();
			if (parameters != null) {
				result += "(";
				for (ISCParameter p : parameters) {
					result += p.getElementName() + " : ";
					result += p.getElementType();
				}
				result += ") \n";
			}
			if (!evt.getLabel().equals("INITIALISATION")){ //INITIALISATION is a bit different
				ArrayList<String> Guards = eventGuards(evt);
				result += "require\n";
				for (String g: Guards){
					result += g + "\n";
				}
				
				result += "actions\n";
				ArrayList<String> Actions = eventActions(evt);
				for (String a: Actions) {
					result += a + "\n";
				}
				
			}
			else if (evt.getLabel().equals("INITIALISATION")) {
				ArrayList<String> InitActions = eventActions(evt);
				for (String a: InitActions) {
					result += a + "\n"; 
				}
			}
			
		}
		return result;
	}

	//Returns contexts
	public ArrayList<String> getContexts(ISCInternalContext[] ctxs) throws RodinDBException {
		ArrayList<String> result = new ArrayList<String>();
		for (ISCInternalContext ctx: ctxs) {
			result.add(ctx.getElementName());
			
			ISCCarrierSet[] carriersets = ctx.getSCCarrierSets();
			result.add("SETS");
			for (ISCCarrierSet set: carriersets) {
				result.add(set.getElementName());
			}
			
			ISCConstant[] constants = ctx.getSCConstants();
			result.add("\nCONSTANTS");
			for (ISCConstant c: constants) {
				result.add(c.getElementName());
			}
			
			ISCAxiom[] axioms = ctx.getSCAxioms();
			result.add("\nAXIOMS");
			for (ISCAxiom ax: axioms) {
				result.add(translation(ax.getPredicateString()));
			}	
		}
		return result;
	}
	
	// Returns the translation in Eiffel of the guards of 'machineName'
	public ArrayList<String> eventGuards(ISCEvent event) throws RodinDBException {
		ArrayList<String> res = new ArrayList<String>(); 
		ISCGuard[] evtGuards = rodinDB.getEvtGuards (event);
		for (int i=0; i<evtGuards.length;i++){
			
			res.add (translation(evtGuards[i].getPredicateString()));			
		}
		return res;
	}
	
	// Returns the translation of EB variables into Eiffel
	public String machineVariables(IRodinFile rodinFile, String machineName) throws RodinDBException {
		StringBuffer res = new StringBuffer("");
		ISCVariable[] variables = rodinDB.getMachineVariables(rodinFile,machineName);
		for (ISCVariable var : variables){
			res.append(variable(var));
			res.append(" ");
		}
		return res.toString();
	}
	
	//Returns translation of variable (var) type from EB to Eiffel.
		public String variable(ISCVariable var) throws RodinDBException{
			final FormulaFactory f = FormulaFactory.getDefault();
			String res = null;
			try {
				res = var.getElementName().toString() + ": " + getType(var.getType(f).toString());
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return res;
		}

		public String getType(String varDef){
			return oper.parseExpression(varDef);
		}
		
		public String translateAssignment(String assig){
			return oper.Assignment(assig);
		}

		// Returns the translation in Eiffel of the actions of 'event'
		public ArrayList<String> eventActions(ISCEvent event) throws CoreException {
			ISCAction[] evtActions =  rodinDB.getEvtActions(event);
			ArrayList<String> res = new ArrayList<String>();
			for (ISCAction evtAction : evtActions){
				res.add(translateAssignment(evtAction.getAssignmentString()));
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

	public String translation (String text){
		String res = "";
		try {
			for (String l: oper.parsePredicate(text)) {
				res += l + "\n";
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