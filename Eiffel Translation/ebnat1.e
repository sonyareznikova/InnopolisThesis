note
	description: "[
		{EBNAT} represents Natural Numbers type in Event-B, it does not contain number 0.
	]"
	author: "Victor Rivera"
	date: ""
	revision: "$Revision$"

class
	EBNAT1

inherit

	EBSET [NATURAL]
		export
			{NONE}
				singleton,
				from_set,
				card,
				partition,
				union,
				intersection,
				difference,
				pow,
				pow1,
				min,
				max
		redefine
			has,
			is_empty,
			finite,
			default_create,
			is_equal,
			partition
		end

feature -- Default Creation

	default_create
		do
			create elements
		end

feature -- Redefinition
	has (v: NATURAL): BOOLEAN
		do
			Result := v >= 1
		end

	is_empty: BOOLEAN
		do
			Result := False
		end

	finite : BOOLEAN
		do
			Result := False
		end

	is_equal (other: EBNAT1) : BOOLEAN
		do
			Result := True
		end

	partition (sets: ARRAY [EBNAT1]): BOOLEAN
			-- FROM EBSET
		do
			Result := False
		end

end
