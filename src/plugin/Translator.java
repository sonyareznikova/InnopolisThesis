package plugin;

import java.util.ArrayList;

import org.eventb.core.ast.Assignment;
import org.eventb.core.ast.AssociativeExpression;
import org.eventb.core.ast.AssociativePredicate;
import org.eventb.core.ast.AtomicExpression;
import org.eventb.core.ast.BecomesEqualTo;
import org.eventb.core.ast.BecomesMemberOf;
import org.eventb.core.ast.BecomesSuchThat;
import org.eventb.core.ast.BinaryExpression;
import org.eventb.core.ast.BinaryPredicate;
import org.eventb.core.ast.BoolExpression;
import org.eventb.core.ast.BoundIdentDecl;
import org.eventb.core.ast.BoundIdentifier;
import org.eventb.core.ast.Expression;
import org.eventb.core.ast.ExtendedExpression;
import org.eventb.core.ast.ExtendedPredicate;
import org.eventb.core.ast.Formula;
import org.eventb.core.ast.FormulaFactory;
import org.eventb.core.ast.FreeIdentifier;
import org.eventb.core.ast.IParseResult;
import org.eventb.core.ast.ISimpleVisitor2;
import org.eventb.core.ast.IntegerLiteral;
import org.eventb.core.ast.LiteralPredicate;
import org.eventb.core.ast.MultiplePredicate;
import org.eventb.core.ast.Predicate;
import org.eventb.core.ast.PredicateVariable;
import org.eventb.core.ast.QuantifiedExpression;
import org.eventb.core.ast.QuantifiedPredicate;
import org.eventb.core.ast.RelationalPredicate;
import org.eventb.core.ast.SetExtension;
import org.eventb.core.ast.SimplePredicate;
import org.eventb.core.ast.UnaryExpression;
import org.eventb.core.ast.UnaryPredicate;

public class Translator implements ISimpleVisitor2 {
	
	//carrying out the translation
	ArrayList<String> eiffelCode;
	
	//carrying out the translation of types
	ArrayList<String> eiffelType;
	
	//constructor
	Translator(){
		eiffelCode = new ArrayList<String>();
	}
	
	public ArrayList<String> parsePredicate(String text){
		//start a new translation
		eiffelCode = new ArrayList<String>();
		
		final FormulaFactory ff = FormulaFactory.getDefault();
		final IParseResult result = 
				ff.parsePredicate(text, null);
		final Predicate p = result.getParsedPredicate();
		p.accept(this);

		return eiffelCode;
	}
	
	public String parseExpression(String expression){
		//start a new translation
		eiffelType = new ArrayList<String>();
		String res = "";
		final FormulaFactory ff = FormulaFactory.getDefault();
		final IParseResult result = 
				ff.parseExpression(expression,null);
		final Expression e = result.getParsedExpression();
		eiffelCode.add(":"+e.toString()+": ");
		e.accept(this);
		for (String i: eiffelType)
			res += i;
		eiffelType = null;
		return res;
	}

	// Translates EventB assignments (var := Expression) into Eiffel
	public String Assignment(String assig){
		//start a new translation
		eiffelCode = new ArrayList<String>();
		String res = "";
		
		final FormulaFactory ff = FormulaFactory.getDefault();
		final IParseResult result = 
				ff.parseAssignment(assig, null);

		final Assignment a = result.getParsedAssignment();	
		a.accept(this);
		
		for (String i: eiffelCode)
			res += i;
		return res;
	}

	//visited
	@Override
	public void visitBecomesEqualTo(BecomesEqualTo assignment) {
		// TODO Auto-generated method stub
		System.out.println("visitBecomesEqualTo");
		FreeIdentifier[] ident = assignment.getAssignedIdentifiers();
		for (FreeIdentifier i: ident)
			eiffelCode.add(i.toString());
		eiffelCode.add(" := ");
		Expression[] exp = assignment.getExpressions();
		for (int i=0; i < exp.length;i++){
			exp[i].accept(this);
		}
		
	}
	//visited
	@Override
	public void visitBecomesMemberOf(BecomesMemberOf assignment) {
		// TODO Auto-generated method stub
		System.out.println("visitBecomesMemberOf");
		FreeIdentifier[] ident = assignment.getAssignedIdentifiers();
		for (FreeIdentifier i: ident)
			eiffelCode.add(i.toString());
		eiffelCode.add(" becomes member of ");
		Expression setExpr= assignment.getSet();
		setExpr.accept(this);
		
	}

	@Override
	public void visitBecomesSuchThat(BecomesSuchThat assignment) {
		// TODO Auto-generated method stub
		System.out.println("visitBecomesSuchThat");
		FreeIdentifier[] ident = assignment.getAssignedIdentifiers();
		for (FreeIdentifier i: ident)
			eiffelCode.add(i.toString());
		eiffelCode.add(" becomes such that ");
		Predicate cnd = assignment.getCondition();
		cnd.accept(this);
				
	}
	
	//visited
	@Override
	public void visitBoundIdentDecl(BoundIdentDecl boundIdentDecl) {
		// TODO Auto-generated method stub
		System.out.println("visitBoundIdentDecl");
		eiffelCode.add(boundIdentDecl.getName());
	}
	
	//visited
	@Override
	public void visitAssociativeExpression(AssociativeExpression expression) {
		// TODO Auto-generated method stub
		// {BUNION, BINTER, BCOMP, FCOMP, OVR, PLUS, MUL, 
		System.out.println("visitAssociativeExpression");
		switch (expression.getTag()) {
		case Formula.BUNION:
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(" union ");	
			}
			break;
		case Formula.BINTER:
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(" intersection ");	
			}
			break;
		case Formula.BCOMP:
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(" bcomp ");	
			}
			break;
		case Formula.FCOMP:
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(" fcomp ");	
			}
			break;
		case Formula.OVR:
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(" ovr ");	
			}
			break;
		case Formula.PLUS:
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(" + ");	
			}
			break;
		case Formula.MUL:
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(" * ");	
			}
			break;
		}
		
	}
	
	//visited
	@Override
	public void visitAtomicExpression(AtomicExpression expression) {
		// TODO to complete
		/*{INTEGER, NATURAL, NATURAL1, BOOL, TRUE, FALSE, EMPTYSET, KPRED, KSUCC,
		  KPRJ1_GEN, KPRJ2_GEN, KID_GEN}.*/
		System.out.println("visitAtomicExpression");
		switch(expression.getTag()) {
		case Formula.INTEGER:
			if (eiffelType != null){ //currently translating a Type
				eiffelType.add("EBINT");
			}
			break;
		case Formula.NATURAL:
			if (eiffelType != null){ //currently translating a Type
				eiffelType.add("EBNAT1");
			}
			break;
		case Formula.NATURAL1:
			if (eiffelType != null){ //currently translating a Type
				eiffelType.add("EBNAT1");
			}
			break;
		case Formula.BOOL:
			eiffelCode.add(expression.toString());
			break;
		case Formula.TRUE:
			eiffelCode.add(expression.toString());
			break;
		case Formula.FALSE:
			eiffelCode.add(expression.toString());
			break;
		case Formula.EMPTYSET:
			eiffelCode.add(" create {EBSET}.empty_set");
			break;
		case Formula.KPRED:
			eiffelCode.add("kpred");
			break;
		case Formula.KSUCC:
			eiffelCode.add("ksucc");
			break;
		case Formula.KPRJ1_GEN:
			eiffelCode.add("kprj1_gen");
			break;
		case Formula.KPRJ2_GEN:
			eiffelCode.add("kprj2_gen");
			break;
		case Formula.KID_GEN:
			eiffelCode.add("kid_gen");
			break;
		}
	}
	
	//visited
	@Override
	public void visitBinaryExpression(BinaryExpression expression) {
		// TODO Auto-generated method stub
		/* {MINUS, MAPSTO, DIV, MOD, EXPN, REL, TREL, SREL, STREL, PFUN
		 	TFUN, PINJ, TINJ, PSUR, TSUR, TBIJ, SETMINUS, DOMRES, DOMSUB,
		 	RANRES, RANSUB, UPTO, RELIMAGE, DPROD, PPROD, FUNIMAGE}*/
		System.out.println("visitBinaryExpression");
		switch(expression.getTag()) {
		case Formula.CPROD:
			expression.getLeft().accept(this);
			eiffelCode.add("x");
			expression.getRight().accept(this);
			break;
		case Formula.MINUS:
			expression.getLeft().accept(this);
			eiffelCode.add("-");
			expression.getRight().accept(this);
			break;
		case Formula.MAPSTO:
			expression.getLeft().accept(this);
			eiffelCode.add("maps to");
			expression.getRight().accept(this);
			break;
		case Formula.DIV:
			expression.getLeft().accept(this);
			eiffelCode.add("/");
			expression.getRight().accept(this);
			break;
		case Formula.MOD:
			expression.getLeft().accept(this);
			eiffelCode.add("\\");
			expression.getRight().accept(this);
			break;
		case Formula.EXPN:
			expression.getLeft().accept(this);
			eiffelCode.add("^");
			expression.getRight().accept(this);
			break;
		case Formula.REL:
			expression.getLeft().accept(this);
			eiffelCode.add("rel");
			expression.getRight().accept(this);
			break;
		case Formula.TREL:
			expression.getLeft().accept(this);
			eiffelCode.add("trel");
			expression.getRight().accept(this);
			break;
		case Formula.SREL:
			expression.getLeft().accept(this);
			eiffelCode.add("srel");
			expression.getRight().accept(this);
			break;
		case Formula.STREL:
			expression.getLeft().accept(this);
			eiffelCode.add("strel");
			expression.getRight().accept(this);
			break;
		case Formula.PFUN:
			expression.getLeft().accept(this);
			eiffelCode.add("pfun");
			expression.getRight().accept(this);
			break;
		case Formula.TFUN:
			expression.getLeft().accept(this);
			eiffelCode.add("tfun");
			expression.getRight().accept(this);
			break;
		case Formula.PINJ:
			expression.getLeft().accept(this);
			eiffelCode.add("pinj");
			expression.getRight().accept(this);
			break;
		case Formula.TINJ:
			expression.getLeft().accept(this);
			eiffelCode.add("tinj");
			expression.getRight().accept(this);
			break;
		case Formula.PSUR:
			expression.getLeft().accept(this);
			eiffelCode.add("psur");
			expression.getRight().accept(this);
			break;
		case Formula.TSUR:
			expression.getLeft().accept(this);
			eiffelCode.add("tsur");
			expression.getRight().accept(this);
			break;
		case Formula.TBIJ:
			expression.getLeft().accept(this);
			eiffelCode.add("tbij");
			expression.getRight().accept(this);
			break;
		case Formula.SETMINUS:
			expression.getLeft().accept(this);
			eiffelCode.add(".difference (");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.DOMRES:
			expression.getLeft().accept(this);
			eiffelCode.add("domres");
			expression.getRight().accept(this);
			break;
		case Formula.DOMSUB:
			expression.getLeft().accept(this);
			eiffelCode.add("domsub");
			expression.getRight().accept(this);
			break;
		case Formula.RANRES:
			expression.getLeft().accept(this);
			eiffelCode.add("ranres");
			expression.getRight().accept(this);
			break;
		case Formula.RANSUB:
			expression.getLeft().accept(this);
			eiffelCode.add("ransub");
			expression.getRight().accept(this);
			break;
		case Formula.UPTO:
			expression.getLeft().accept(this);
			eiffelCode.add("upto");
			expression.getRight().accept(this);
			break;
		case Formula.RELIMAGE:
			expression.getLeft().accept(this);
			eiffelCode.add("relimage");
			expression.getRight().accept(this);
			break;
		case Formula.DPROD:
			expression.getLeft().accept(this);
			eiffelCode.add("dprod");
			expression.getRight().accept(this);
			break;
		case Formula.PPROD:
			expression.getLeft().accept(this);
			eiffelCode.add("pprod");
			expression.getRight().accept(this);
			break;
		case Formula.FUNIMAGE:
			expression.getLeft().accept(this);
			eiffelCode.add("funimage");
			expression.getRight().accept(this);
			break;
		}
		
	}
	
	//visited
	@Override
	public void visitBoolExpression(BoolExpression expression) {
		//bool(predicate)
		// TODO Auto-generated method stub
		System.out.println("visitBoolExpression");
		eiffelCode.add("bool(");
		expression.getPredicate().accept(this);
		eiffelCode.add(")");
	}

	//visited
	@Override
	public void visitIntegerLiteral(IntegerLiteral expression) {
		// 0..9
		// TODO Auto-generated method stub
		System.out.println("visitIntegerLiteral");
		eiffelCode.add(expression.getValue().toString());
		
	}
	
	//visited
	@Override
	public void visitQuantifiedExpression(QuantifiedExpression expression) {
		// {union (bigU), intersection (bign)} varlist . predicate | expression
		// TODO Auto-generated method stub
		System.out.println("visitQuantifiedExpression");
		int n_children = expression.getChildCount();
		switch (expression.getTag()) {
		case Formula.QUNION:
			eiffelCode.add("qunion");
			for (int i=0;i<n_children;i++) {
				expression.getChild(i).accept(this);
			}
			break;
		case Formula.QINTER:
			eiffelCode.add("qinter");
			for (int i=0;i<n_children;i++) {
				expression.getChild(i).accept(this);
			}
			break;
		//do not implement
		case Formula.CSET:
			eiffelCode.add("cset");
			for (int i=0;i<n_children;i++) {
				expression.getChild(i).accept(this);
			}
			break;
		}
		
		
	}
	
	//visited
	@Override
	public void visitSetExtension(SetExtension expression) {
		// '{' list_expression '}'
		// TODO Auto-generated method stub
		System.out.println("visitSetExtension");
		int n_children = expression.getChildCount();
		eiffelCode.add("{");
		for (int i = 0; i < n_children; i++) {
			expression.getChild(i).accept(this);
		}
		eiffelCode.add("}");
		
	}
	
	//visited
	@Override
	public void visitUnaryExpression(UnaryExpression expression) {
		//	{UNMINUS, POW, POW1, KUNION, KINTER, KDOM, KRAN, KMIN, KMAX, CONVERSE, KPRJ1_GEN
		// KPRJ2_GEN, KID_GEN}
		// TODO Auto-generated method stub
		System.out.println("visitUnaryExpression");
		switch(expression.getTag()) {
		case Formula.UNMINUS:
			eiffelCode.add("unminus");
			expression.getChild().accept(this);
			break;
		case Formula.POW:
			eiffelCode.add("pow");
			expression.getChild().accept(this);
			break;
		case Formula.POW1:
			eiffelCode.add("pow1");
			expression.getChild().accept(this);
			break;
		case Formula.KUNION:
			eiffelCode.add("kunion");
			expression.getChild().accept(this);
			break;
		case Formula.KINTER:
			eiffelCode.add("kinter");
			expression.getChild().accept(this);
			break;
		case Formula.KDOM:
			eiffelCode.add("dom(");
			expression.getChild().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.KRAN:
			eiffelCode.add("range");
			expression.getChild().accept(this);
			break;
		case Formula.KMIN:
			eiffelCode.add("kmin");
			expression.getChild().accept(this);
			break;
		case Formula.KMAX:
			eiffelCode.add("kmax");
			expression.getChild().accept(this);
			break;
		case Formula.CONVERSE:
			eiffelCode.add("converse");
			expression.getChild().accept(this);
			break;
		case Formula.KPRJ1_GEN:
			eiffelCode.add("kprj1_gen");
			expression.getChild().accept(this);
			break;
		case Formula.KPRJ2_GEN:
			eiffelCode.add("kprj2_gen");
			expression.getChild().accept(this);
			break;
		case Formula.KID_GEN:
			eiffelCode.add("kid_gen");
			expression.getChild().accept(this);
			break;
		}
		
	}
	
	//???
	@Override
	public void visitBoundIdentifier(BoundIdentifier identifierExpression) {
		// TODO Auto-generated method stub
		System.out.println("visitBoundIdentifier");	
	}
	
	//visited
	@Override
	public void visitFreeIdentifier(FreeIdentifier identifierExpression) {
		// TODO Auto-generated method stub
		System.out.println("visitFreeIdentifier");
		eiffelCode.add(identifierExpression.getName());
	}

	//visited
	@Override
	public void visitAssociativePredicate(AssociativePredicate predicate) {
		System.out.println("visitAssociativePredicate");
		switch (predicate.getTag()){
		case Formula.LAND:
			int i = predicate.getChildren().length;
			for (Predicate pre : predicate.getChildren()){
				// Visit each expression
				pre.accept(this);
				if (i > 1){
					eiffelCode.add(" and ");
				}
				i--;
			}
			break;
		case Formula.LOR:
			int i2 = predicate.getChildren().length;
			for (Predicate pre : predicate.getChildren()){
				pre.accept(this);
				if (i2 > 1){
					eiffelCode.add(" or ");
				}i2--;
			}
			break;
		}
	}
	
	//visited
	@Override
	public void visitBinaryPredicate(BinaryPredicate predicate) {
		// {LIMP, LEQV}
		// TODO Auto-generated method stub
		System.out.println("visitBinaryPredicate");
		switch (predicate.getTag()) {
		case Formula.LIMP:
			predicate.getLeft().accept(this);
			eiffelCode.add("implies");
			predicate.getRight().accept(this);
			break;
		case Formula.LEQV:
			predicate.getLeft().accept(this);
			eiffelCode.add(".equals (");
			predicate.getRight().accept(this);
			eiffelCode.add(")");
			break;
		}
		
	}
	
	//visited 
	@Override
	public void visitLiteralPredicate(LiteralPredicate predicate) {
		// {TRUE, FALSE}
		// TODO Auto-generated method stub
		System.out.println("visitLiteralPredicate");
		switch (predicate.getTag()) {
		case Formula.BTRUE:
			eiffelCode.add("True");
			break;
		case Formula.BFALSE:
			eiffelCode.add("False");
			break;
		}
	}
	
	//visited
	@Override
	public void visitMultiplePredicate(MultiplePredicate predicate) {
		//partition(a_1,a_2,...,a_n)
		// TODO Auto-generated method stub
		System.out.println("visitMultiplePredicate");
		Expression[] children = predicate.getChildren();
		eiffelCode.add("partition(");
		for (Expression e: children) {
			e.accept(this);
		}
		eiffelCode.add(")");	
	}
	
	//visited
	@Override
	public void visitQuantifiedPredicate(QuantifiedPredicate predicate) {
		//{forall/exists} varlist . predicate
		// TODO Auto-generated method stub
		System.out.println("visitQuantifiedPredicate");
		int n_children = predicate.getChildCount();
		switch (predicate.getTag()) {
		case Formula.FORALL:
			eiffelCode.add(".for_all");
			for (int i = 0; i < n_children; i++) {
				predicate.getChild(i).accept(this);
			}
			BoundIdentDecl[] bndind = predicate.getBoundIdentDecls();
			for (BoundIdentDecl bd: bndind) {
				bd.accept(this);
			}
			break;
		case Formula.EXISTS:
			eiffelCode.add(".exists");
			for (int i = 0; i < n_children; i++) {
				predicate.getChild(i).accept(this);
			}
			BoundIdentDecl[] bndind1 = predicate.getBoundIdentDecls();
			for (BoundIdentDecl bd: bndind1) {
				bd.accept(this);
			}
			break;	
		}
	}
	
	//visited
	@Override
	public void visitRelationalPredicate(RelationalPredicate predicate) {
		// expression	{= / <= / < / >= / > / : (belongs)} expression
		System.out.println("visitRelationalPredicate");
		//for (int i = 0; i < predicate.getSyntacticallyFreeIdentifiers().length; i++) {
		//	System.out.println(predicate.getSyntacticallyFreeIdentifiers()[i]);
		//}
		switch (predicate.getTag()){
		case 	Formula.EQUAL:
			predicate.getLeft().accept(this);
			eiffelCode.add(" = ");
			predicate.getRight().accept(this);
			break;
		case 	Formula.NOTEQUAL:
			predicate.getLeft().accept(this);
			eiffelCode.add(" != ");
			predicate.getRight().accept(this);
			break;
		case    Formula.LT:
			predicate.getLeft().accept(this);
			eiffelCode.add(" < ");
			predicate.getRight().accept(this);
			break;
		case	    Formula.LE:
			predicate.getLeft().accept(this);
			eiffelCode.add(" <= ");
			predicate.getRight().accept(this);
			break;
		case	    Formula.GT:
			predicate.getLeft().accept(this);
			eiffelCode.add(" > ");
			predicate.getRight().accept(this);
			break;
		case 	Formula.GE:
			predicate.getLeft().accept(this);
			eiffelCode.add(" >= ");
			predicate.getRight().accept(this);
			break;
		case 	Formula.IN:
			predicate.getLeft().accept(this);
			eiffelCode.add("belongs");
			predicate.getRight().accept(this);
			break;
		case 	Formula.NOTIN:
			predicate.getLeft().accept(this);
			eiffelCode.add("not in");
			predicate.getRight().accept(this);
			break;
		case 	Formula.SUBSET:
			predicate.getLeft().accept(this);
			eiffelCode.add("a subset of");
			predicate.getRight().accept(this);
			break;
		case 	Formula.NOTSUBSET:
			predicate.getLeft().accept(this);
			eiffelCode.add("!subset of");
			predicate.getRight().accept(this);
			break;
		case 	Formula.SUBSETEQ:
			predicate.getLeft().accept(this);
			eiffelCode.add("subset or equals");
			predicate.getRight().accept(this);
			break;
		case 	Formula.NOTSUBSETEQ:
			predicate.getLeft().accept(this);
			eiffelCode.add("!subset or equals");
			predicate.getRight().accept(this);
			break;
		}
		
	}
	
	//visited
	@Override
	public void visitSimplePredicate(SimplePredicate predicate) {
		// finite (expression)
		// TODO Auto-generated method stub
		System.out.println("visitSimplePredicate");
		eiffelCode.add("finite(");
		predicate.getExpression().accept(this);
		eiffelCode.add(")");
	}
	
	//visited
	@Override
	public void visitUnaryPredicate(UnaryPredicate predicate) {
		// TODO Auto-generated method stub
		System.out.println("visitUnaryPredicate");
		eiffelCode.add("not");
		predicate.getChild().accept(this);
	}
	
	//???
	@Override
	public void visitExtendedExpression(ExtendedExpression expression) {
		// TODO Auto-generated method stub
		System.out.println("visitExtendedExpression");
		
	}
	
	//???
	@Override
	public void visitExtendedPredicate(ExtendedPredicate predicate) {
		// TODO Auto-generated method stub
		System.out.println("visitExtendedPredicate");
		
	}

	//visited
	@Override
	public void visitPredicateVariable(PredicateVariable predVar) {
		// TODO Auto-generated method stub
		System.out.println("visitPredicateVariable");
		eiffelCode.add(predVar.getName());
		
	}

}
