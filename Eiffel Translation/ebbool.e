note
	description: "[
		{EBBOOL} represents BOOLEAN type in Event-B.
	]"
	author: "Victor Rivera"
	date: ""
	revision: "$Revision$"

class
	EBBOOL

inherit

	EBSET [BOOLEAN]
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
			default_create,
			is_equal,
			partition
		end

feature -- Default Creation

	default_create
		do
			create elements
			elements.extend_front (True)
			elements.extend_front (False)
		end

feature -- Redefinition
	has (v: BOOLEAN): BOOLEAN
		do
			Result := True
		end

	is_empty: BOOLEAN
		do
			Result := False
		end

	is_equal (other: EBBOOL) : BOOLEAN
		do
			Result := True
		end

	partition (sets: ARRAY [EBBOOL]): BOOLEAN
			-- FROM EBSET
		do
			Result := False
		end
end
