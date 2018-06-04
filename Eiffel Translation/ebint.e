note
	description: "[
		{EBINT} represents Integers type in Event-B.
	]"
	author: "Victor Rivera"
	date: ""
	revision: "$Revision$"

class
	EBINT

inherit

	EBSET [INTEGER]
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
create
	default_create,
	from_set

feature -- Default Creation

	default_create
		do
			create elements
		end

feature -- Redefinition
	has (v: INTEGER): BOOLEAN
		do
			Result := True
		end

	is_empty: BOOLEAN
		do
			Result := False
		end

	finite : BOOLEAN
		do
			Result := False
		end

	is_equal (other: EBINT) : BOOLEAN
		do
			Result := True
		end

	partition (sets: ARRAY [EBINT]): BOOLEAN
			-- FROM EBSET
		do
			Result := False
		end

end
