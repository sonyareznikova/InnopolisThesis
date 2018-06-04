note
	description: "[
		{EBRANGE} represents Range of Integers type in Event-B.
	]"
	author: "Victor Rivera"
	date: ""
	revision: "$Revision$"

class
	EBRANGE

inherit

	EBSET [INTEGER]
		export
			{NONE}
				singleton,
				from_set
		redefine
			min,
			max,
			is_equal,
			partition
		end

create
	make

feature -- Initialisation

	make (l, u: INTEGER)
		do
			create elements
			if l <= u then
				across
					l |..| u as v
				loop
					elements.extend_front (v.item)
				end
			end
			lower := l
			upper := u
		end

feature -- Redefinition

	min: INTEGER
		do
			Result := lower
		end

	max: INTEGER
		do
			Result := upper
		end

	is_equal (other: EBRANGE) : BOOLEAN
		do
			Result := Precursor (other)
		end

	partition (sets: ARRAY [EBRANGE]): BOOLEAN
		do
			Result := Precursor (sets)
		end

feature -- Access
	lower, upper: INTEGER
		-- the bounds of the range

invariant
	across elements as e all lower <= e.item and e.item <= upper end

end
