note
	description: ""
	author: "Victor Rivera"
	date: ""
	revision: "$Revision$"

class
	PERSONS

inherit

	EBSET [PERSONS]
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
	make (n: STRING)
		do
			default_create
			name := n
		end

feature -- Default Creation

	default_create
		do
			create elements
			is_user_type_def := True
		end

feature -- Redefinition
	has (v: PERSONS): BOOLEAN
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

	union (other: EBSET [PERSONS]): EBSET [PERSONS]
			-- FROM EBSET
		do
			Result := Current
		end

	intersection (other: EBSET [PERSONS]): EBSET [PERSONS]
			-- FROM EBSET
		do
			Result := Current
		end

	difference (other: EBSET [PERSONS]): EBSET [PERSONS]
			-- FROM EBSET
		do
			Result := Current
		end

	is_equal (other: PERSONS) : BOOLEAN
		do
			Result := True
		end

	partition (sets: ARRAY [PERSONS]): BOOLEAN
		do
			Result := False
		end

feature -- Added by the user
	name: detachable STRING

	out: STRING
		do
			if attached name as n then
				Result := n
			else
				Result := ">> No name attached"
			end
		end
end
