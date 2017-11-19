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
import org.eventb.core.ast.ITypeEnvironmentBuilder;
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
		//System.out.println(e.toString());
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
		eiffelCode.add(".assigns ( ");
		Expression[] exp = assignment.getExpressions();
		for (int i=0; i < exp.length;i++){
			exp[i].accept(this);
		}
		eiffelCode.add(")");
		
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
				if (i != expression.getChildCount()-1) eiffelCode.add(".union (");	
			}
			eiffelCode.add(")");
			break;
		case Formula.BINTER:
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(".intersection ( ");	
			}
			eiffelCode.add(")");
			break;
		case Formula.BCOMP:
			eiffelCode.add("create {EBREL_OPER[_,_]}.backward (");
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(", ");	
			}
			eiffelCode.add(")");
			break;
		case Formula.FCOMP:
			eiffelCode.add("create {EBREL_OPER[_,_]}.forward (");
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(", ");	
			}
			eiffelCode.add(")");
			break;
		case Formula.OVR:
			eiffelCode.add("create {EBREL_OPER[_,_]}.override (");
			for (int i=0;i<expression.getChildCount();i++) {
				expression.getChild(i).accept(this);
				if (i != expression.getChildCount()-1) eiffelCode.add(", ");	
			}
			eiffelCode.add(")");
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
				break;
			}
			eiffelCode.add("EBINT");
			break;
		case Formula.NATURAL:
			if (eiffelType != null){ //currently translating a Type
				eiffelType.add("EBNAT");
				break;
			}
			eiffelCode.add("EBNAT");
			break;
		case Formula.NATURAL1:
			if (eiffelType != null){ //currently translating a Type
				eiffelType.add("EBNAT1");
				break;
			}
			eiffelCode.add("EBNAT1");
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
			eiffelCode.add("create {EBREL [_,_]}.cartesian_prod (");
			expression.getLeft().accept(this);
			eiffelCode.add(", ");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.MINUS:
			expression.getLeft().accept(this);
			eiffelCode.add(" - ");
			expression.getRight().accept(this);
			break;
		case Formula.MAPSTO:
			eiffelCode.add(" (create {EBPAIR[_,_]}.make (");
			expression.getLeft().accept(this);
			eiffelCode.add(", ");
			expression.getRight().accept(this);
			eiffelCode.add("))");
			break;
		case Formula.DIV:
			expression.getLeft().accept(this);
			eiffelCode.add(" / ");
			expression.getRight().accept(this);
			break;
		case Formula.MOD:
			expression.getLeft().accept(this);
			eiffelCode.add(" \\ ");
			expression.getRight().accept(this);
			break;
		case Formula.EXPN:
			expression.getLeft().accept(this);
			eiffelCode.add(" ^ ");
			expression.getRight().accept(this);
			break;
		case Formula.REL:
			expression.getLeft().accept(this);
			eiffelCode.add(" rel ");
			expression.getRight().accept(this);
			break;
		case Formula.TREL:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_total_rel");
			break;
		case Formula.SREL:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_surj_rel");
			break;
		case Formula.STREL:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_tsurj_rel");
			break;
		case Formula.PFUN:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_partial_func");
			break;
		case Formula.TFUN:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_total_func");
			break;
		case Formula.PINJ:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_pinj_func");
			break;
		case Formula.TINJ:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_tinj_func");
			break;
		case Formula.PSUR:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_psurj_func");
			break;
		case Formula.TSUR:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_tsurj_func");
			break;
		case Formula.TBIJ:
			eiffelCode.add("(");
			expression.getLeft().accept(this);
			expression.getRight().accept(this);
			eiffelCode.add(").is_biject_func");
			break;
		case Formula.SETMINUS:
			expression.getLeft().accept(this);
			eiffelCode.add(".difference (");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.DOMRES:
			expression.getLeft().accept(this);
			eiffelCode.add(".domain_restriction (");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.DOMSUB:
			expression.getRight().accept(this);
			eiffelCode.add(".domain_subtraction (");
			expression.getLeft().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.RANRES:
			expression.getLeft().accept(this);
			eiffelCode.add(".range_restriction (");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.RANSUB:
			expression.getLeft().accept(this);
			eiffelCode.add(".range_substraction (");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.UPTO:
			expression.getLeft().accept(this);
			eiffelCode.add(" upto ");
			expression.getRight().accept(this);
			break;
		case Formula.RELIMAGE:
			expression.getLeft().accept(this);
			eiffelCode.add(".rel_image(");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.DPROD:
			eiffelCode.add("create {EBREL_OPER [_, EBREL[_,_]]}.direct_product (");
			expression.getLeft().accept(this);
			eiffelCode.add(", ");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.PPROD:
			eiffelCode.add("create {EBREL_OPER [EBREL [_,_], EBREL [_,_]]}.parallel_product ");
			expression.getLeft().accept(this);
			eiffelCode.add(", ");
			expression.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case Formula.FUNIMAGE:
			expression.getLeft().accept(this);
			eiffelCode.add(".apply (");
			expression.getRight().accept(this);
			eiffelCode.add(")");
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
			eiffelCode.add(".qunion (");
			for (int i=0;i<n_children;i++) {
				if (i != n_children-1) {
					expression.getChild(i).accept(this);
					eiffelCode.add(", ");
				}
				else expression.getChild(i).accept(this);
			}
			eiffelCode.add(")");
			break;
		case Formula.QINTER:
			eiffelCode.add(".qinter (");
			for (int i=0;i<n_children;i++) {
				if (i != n_children-1) {
					expression.getChild(i).accept(this);
					eiffelCode.add(", ");
				}
				else expression.getChild(i).accept(this);
			}
			eiffelCode.add(")");
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
		//
		System.out.println("visitSetExtension");
		int n_children = expression.getChildCount();
		System.out.println(n_children);
		for (int i=0;i<n_children;i++) {
			if (expression.getChild(i) instanceof FreeIdentifier) {
				eiffelCode.add("create {EBSET[_]}.singleton (");
				expression.getChild(i).accept(this);
				eiffelCode.add(")");
			}
			else {
				eiffelCode.add("create EBREL[_,_]}.vals (<<");
				expression.getChild(i).accept(this);
				eiffelCode.add(">>)");
			}
		}
		
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
			eiffelCode.add("(");
			expression.getChild().accept(this);
			eiffelCode.add(").power_set");
			break;
		case Formula.POW1:
			eiffelCode.add("(");
			expression.getChild().accept(this);
			eiffelCode.add(").power_set1");
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
			eiffelCode.add("(");
			expression.getChild().accept(this);
			eiffelCode.add(").domain");
			break;
		case Formula.KRAN:
			eiffelCode.add("(");
			expression.getChild().accept(this);
			eiffelCode.add(").range");
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
			eiffelCode.add("(");
			expression.getChild().accept(this);
			eiffelCode.add(").converse");
			break;
		case Formula.KPRJ1_GEN:
			eiffelCode.add("(");
			expression.getChild().accept(this);
			eiffelCode.add(").prj1");
			break;
		case Formula.KPRJ2_GEN:
			eiffelCode.add("(");
			expression.getChild().accept(this);
			eiffelCode.add(").prj2");
			break;
		case Formula.KID_GEN:
			eiffelCode.add("(");
			expression.getChild().accept(this);
			eiffelCode.add(".identity");
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
		if (eiffelType != null) {
			eiffelType.add(identifierExpression.getName());
		}
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
			eiffelCode.add(".is_equal (");
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
			eiffelCode.add(".is_equal (");
			predicate.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case 	Formula.NOTEQUAL:
			predicate.getLeft().accept(this);
			eiffelCode.add(" /= ");
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
			predicate.getRight().accept(this);
			eiffelCode.add(".has (");
			predicate.getLeft().accept(this);
			eiffelCode.add(")");
			break;
		case 	Formula.NOTIN:
			eiffelCode.add("not ");
			predicate.getRight().accept(this);
			eiffelCode.add(".has (");
			predicate.getLeft().accept(this);
			eiffelCode.add(")");
			break;
		case 	Formula.SUBSET:
			predicate.getLeft().accept(this);
			eiffelCode.add(".is_strict_subset (");
			predicate.getRight().accept(this);
			eiffelCode.add(")");
		case    Formula.SUBSETEQ:
			predicate.getLeft().accept(this);
			eiffelCode.add(".is_subset (");
			predicate.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case 	Formula.NOTSUBSET:
			eiffelCode.add("not ");
			predicate.getLeft().accept(this);
			eiffelCode.add(".is_strict_subset (");
			predicate.getRight().accept(this);
			eiffelCode.add(")");
			break;
		case 	Formula.NOTSUBSETEQ:
			eiffelCode.add("not");
			predicate.getLeft().accept(this);
			eiffelCode.add("is_subset (");
			predicate.getRight().accept(this);
			eiffelCode.add(")");
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
