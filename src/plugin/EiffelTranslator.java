package plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EiffelTranslator {
	
	String two_tabs = "\t\t";
	
	public void translateCarrierSets(String name) throws IOException {
		
		//File f = new File(name + ".e");
		//FileWriter fw = new FileWriter(f);
		//BufferedWriter writer = new BufferedWriter(fw);
		
		String class_definition = "class\n\t" + name +"\n";
		
		String inherit_statement = "inherit\n" + 
				"\n" + 
				"	EBSET [" + name + "]\n" + 
				"		export\n" + 
				"			{NONE}\n" + 
				"				singleton,\n" + 
				"				from_set,\n" + 
				"				card,\n" + 
				"				partition,\n" + 
				"				pow,\n" + 
				"				pow1,\n" + 
				"				min,\n" + 
				"				max\n" + 
				"		redefine\n" + 
				"			has,\n" + 
				"			is_empty,\n" + 
				"			default_create,\n" + 
				"			finite,\n" + 
				"			union,\n" + 
				"			intersection,\n" + 
				"			difference,\n" + 
				"			is_equal,\n" + 
				"			partition,\n" + 
				"			-- Added by the user\n" + 
				"			out\n" + 
				"		end";
		
		String create = "create\n" + 
				"	default_create,\n" + 
				"	-- Added by the user\n" + 
				"	make";
		
		String feature_defaults = "create\n" + 
				"	default_create,\n" + 
				"	-- Added by the user\n" + 
				"	make\n" + 
				"\n" + 
				"feature -- Added by the user\n" + 
				"	make (c : STRING)\n" + 
				"		do\n" + 
				"			default_create\n" + 
				"			cont := c\n" + 
				"		end";
		
		String feature_redefinition = "feature -- Redefinition\n" + 
				"	has (v:" + name + "): BOOLEAN\n" + 
				"		do\n" + 
				"			Result := True\n" + 
				"		end\n" + 
				"\n" + 
				"	is_empty: BOOLEAN\n" + 
				"		do\n" + 
				"			Result := False\n" + 
				"		end\n" + 
				"\n" + 
				"	finite: BOOLEAN\n" + 
				"		do\n" + 
				"			Result := False\n" + 
				"		end\n" + 
				"\n" + 
				"	union (other: EBSET [" + name + "]): EBSET [" + name + "]\n" + 
				"			-- FROM EBSET\n" + 
				"		do\n" + 
				"			Result := Current\n" + 
				"		end\n" + 
				"\n" + 
				"	intersection (other: EBSET [" + name + "]): EBSET [" + name + "]\n" + 
				"			-- FROM EBSET\n" + 
				"		do\n" + 
				"			Result := Current\n" + 
				"		end\n" + 
				"\n" + 
				"	difference (other: EBSET [" + name + "]): EBSET [" + name + "]\n" + 
				"			-- FROM EBSET\n" + 
				"		do\n" + 
				"			Result := Current\n" + 
				"		end\n" + 
				"\n" + 
				"	is_equal (other:" + name + ") : BOOLEAN\n" + 
				"			-- FROM EBSET\n" + 
				"		do\n" + 
				"			Result := True\n" + 
				"		end\n" + 
				"\n" + 
				"	partition (sets: ARRAY [" + name + "]): BOOLEAN\n" + 
				"			-- FROM EBSET\n" + 
				"		do\n" + 
				"			Result := False\n" + 
				"		end";
		
		String feature_user_added = "feature -- Added by the user\n" + 
				"	cont : detachable STRING\n" + 
				"\n" + 
				"	out: STRING\n" + 
				"		do\n" + 
				"			if attached cont as n then\n" + 
				"				Result := n\n" + 
				"			else\n" + 
				"				Result := \">> No content attached\"\n" + 
				"			end\n" + 
				"		end\n" + 
				"\n" + 
				"end";
		
		
		//writer.write("test");
		//writer.write(inherit_statement);
		//writer.close();
		//fw.close();
		
		System.out.println(class_definition);
		System.out.println(inherit_statement);
		System.out.println(create);
		System.out.println(feature_defaults);
		System.out.println(feature_redefinition);
		System.out.println(feature_user_added);
		
	}
	
}


