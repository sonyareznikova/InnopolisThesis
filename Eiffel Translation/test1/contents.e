note
	description: ""
	author: "Victor Rivera"
	date: ""
	revision: "$Revision$"

class
	CONTENTS

inherit

	EBSET [CONTENTS]
		export
			{NONE}
				singleton,
				from_set,
				card,
				partition,
				pow,
				pow1,
				min,
				max
		redefine
			has,
			is_empty,
			default_create,
			finite,
			union,
			intersection,
			difference,
			is_equal,
			partition,
			-- Added by the user
			out
		end

create
	default_create,
	-- Added by the user
	make

feature -- Added by the user
	make (c : STRING)
		do
			default_create
			cont := c
		end

feature -- Default Creation

	default_create
		do
			create elements
			is_user_type_def := True
		end

feature -- Redefinition
	has (v: CONTENTS): BOOLEAN
		do
			Result := True
		end

	is_empty: BOOLEAN
		do
			Result := False
		end

	finite: BOOLEAN
		do
			Result := False
		end

	union (other: EBSET [CONTENTS]): EBSET [CONTENTS]
			-- FROM EBSET
		do
			Result := Current
		end

	intersection (other: EBSET [CONTENTS]): EBSET [CONTENTS]
			-- FROM EBSET
		do
			Result := Current
		end

	difference (other: EBSET [CONTENTS]): EBSET [CONTENTS]
			-- FROM EBSET
		do
			Result := Current
		end

	is_equal (other: CONTENTS) : BOOLEAN
			-- FROM EBSET
		do
			Result := True
		end

	partition (sets: ARRAY [CONTENTS]): BOOLEAN
			-- FROM EBSET
		do
			Result := False
		end

feature -- Added by the user
	cont : detachable STRING

	out: STRING
		do
			if attached cont as n then
				Result := n
			else
				Result := ">> No content attached"
			end
		end

end
