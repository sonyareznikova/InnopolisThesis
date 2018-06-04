note
	description: "[
		{EBSET} implements the mathematical language for sets of Event-B.
	]"
	author: "Victor Rivera"
	date: ""
	revision: "$Revision$"
	model: set

class
	EBSET [G]

inherit

	ANY
		redefine
			out, default_create, is_equal
		end

create
	from_set, singleton, default_create, empty_set

feature -- Initialisation

	empty_set, default_create
			-- initialises an empty set
		do
			create elements
		end

	singleton (v: G)
			-- initialises Current with element 'v'
		do
			create elements
			elements.extend_front (v)
		end

	from_set (other: EBSET [G])
			-- initialises Current with elements in 'other'
		local
			i: INTEGER
		do
			create elements
			from
				i := 1
			until
				i > other.set.count
			loop
				elements.extend_front (other.set.item (i))
				i := i + 1
			end
			is_user_type_def := other.is_user_type_def
		end

feature -- Queries

	is_equal (other: EBSET [G]) : BOOLEAN
			-- FROM ANY
		local
			i, j: INTEGER
		do
			if elements.count = other.card then
				from
					i := 1
				until
					i > elements.count or else
					Result
				loop
					from
						j := 1
					until
						j > elements.count or else
						attached (elements [i]) as e and then
							attached other.set[j] as o and then e.is_equal (o)
					loop
						j := j + 1
					end
					Result := j > elements.count
					i := i + 1
				end
				Result := not Result
			end
		end

	is_empty: BOOLEAN
			-- Is the current set empty?
		do
			Result := elements.is_empty
		end

	has (v: G): BOOLEAN
			-- Does the Current set have 'v'?
		do
			Result := elements.sequence.range.has (v)
		end

	card: INTEGER
			-- What is the cardinality of the Current set?
		do
			Result := elements.count
		end

	is_subset (other: EBSET [G]): BOOLEAN
			-- Is the Current set a subset of 'other'?
		local
			i, j: INTEGER
		do
				--other can be a set defined by the user
			if other.is_user_type_def then
				Result := True
			else
				--Result := elements.sequence.range.is_subset_of (other.set.range)
				if card <= other.card then
					from
						i := 1
					until
						i > elements.count or else
						Result
					loop
						from
							j := 1
						until
							j > other.card or else
							(attached elements.item (i) as t and then
								attached other.set [j] as a and then t.is_equal (a))
						loop
							j := j + 1
						end
						Result := j > other.card
						i := i + 1
					end
					Result := not Result
				end
			end
		end

	is_not_subset (other: EBSET [G]): BOOLEAN
			-- Is the Current set not a subset of 'other'?
		do
			Result := not is_subset (other)
		end

	is_strict_subset (other: EBSET [G]): BOOLEAN
			-- Is the Current set a strict subset of 'other'?
		do
				--other can be a set defined by the user
			if other.is_user_type_def then
				Result := True
			else
				Result := is_subset (other) and card /= other.card
			end
		end

	is_not_strict_subset (other: EBSET [G]): BOOLEAN
			-- Is the Current set not a strict subset of 'other'?
		do
			Result := not is_strict_subset (other)
		end

	finite: BOOLEAN
			-- indicates if the Current set is finite
		do
			Result := True
		end

	partition (sets: ARRAY [EBSET [G]]): BOOLEAN
			-- states that the sets in 'sets' constitutes a partition of Current:
			-- the union of all elements in 'sets' is Current, and all elements are disjoint
		local
			sets_union: EBSET [G]
			i: INTEGER
		do
			create sets_union
			from
				i := 1
			until
				i > sets.count or else Result
			loop
				Result := sets_union.intersection (sets [i]).card /= 0
				sets_union := sets_union.union (sets [i])
				i := i + 1
			end
			Result := not Result and then is_equal (sets_union)
		ensure
				--TODO
		end

feature -- Modification (noside-effect)

	union (other: EBSET [G]): EBSET [G]
			-- gives the union of the elements of other and Current
		local
			i: INTEGER
		do
			create Result.from_set (Current)

			from
				i := 1
			until
				i > other.card
			loop
				Result.add (other.set [i])
				i := i + 1
			end
		ensure
			all_elements: is_subset (Result) and other.is_subset (Result)
			no_new_elements: across 1 |..| Result.card as ith all has (Result.set [ith.item]) or other.has (Result.set [ith.item]) end
		end

	intersection (other: EBSET [G]): EBSET [G]
			-- gives the intersection of the elements of other and Current
		do
			create Result
			across
				elements as e
			loop
				if other.has (e.item) then
					Result.add (e.item)
				end
			end
		ensure
			inter_elements: across 1 |..| Result.card as ith all has (Result.set [ith.item]) and other.has (Result.set [ith.item]) end
		end

	difference (other: EBSET [G]): EBSET [G]
			-- gives the difference of the elements of Current and other
		do
			create Result
			across
				elements as e
			loop
				if not other.has (e.item) then
					Result.add (e.item)
				end
			end
		ensure
			inter_elements: across 1 |..| Result.card as ith all has (Result.set [ith.item]) and not other.has (Result.set [ith.item]) end
		end

	pow: EBSET [EBSET [G]]
			-- gives the power set of the Current set
			--TODO: sets are unordered
		local
			new_ps: EBSET [EBSET [G]]
			new_subset: EBSET [G]
			i: INTEGER
		do
			create Result
			Result.add (create {EBSET [G]})
			across
				elements as e
			loop
				create new_ps
				from
					i := 1
				until
					i > Result.card
				loop
					new_ps.add (Result.set.item (i))
					create new_subset.from_set (Result.set.item (i))
					new_subset.add (e.item)
					new_ps.add (new_subset)
					i := i + 1
				end
				Result := new_ps
			end
		ensure
			across 1 |..| Result.card as e all Result.set.item (e.item).is_subset (Current) end
		end

	pow1: EBSET [EBSET [G]]
			-- gives the power set of the Current set (empty_set does not belong)
			--TODO: sets are unordered
		local
			new_ps: EBSET [EBSET [G]]
		do
			create new_ps
			Result := pow.difference (new_ps)
		end

		--gen_union (sets: ARRAY [EBSET[G]]): EBSET[G]
		-- gives the union of all sets in 'sets'
		--TODO it should not be here.. it should be general
		-- same with gen_inter, UNION, INTER
		-- cartesian product

	min: INTEGER
			-- gives the smallest number in the current set
		require
			set_of_numbers: attached {EBSET [INTEGER]} Current
			at_least_one_element: not is_empty
		do
			if attached {V_LINKED_LIST [INTEGER]} elements as elems then
				Result := elems.first
				across
					elems as e
				loop
					if e.item < Result then
						Result := e.item
					end
				end
			end
		ensure
			--across 1 |..| card as e all Result <= set [e.item] end
		end

	max: INTEGER
			-- gives the largest number in the current set
		require
			set_of_numbers: attached {EBSET [INTEGER]} Current
			at_least_one_element: not is_empty
		do
			if attached {V_LINKED_LIST [INTEGER]} elements as elems then
				Result := elems.first
				across
					elems as e
				loop
					if e.item > Result then
						Result := e.item
					end
				end
			end
		end

feature {EBSET} -- Modification (side-effect)

	add (v: G)
			-- adds element 'v' to Current set
		do
			if not elements.has (v) then
				elements.extend_back (v)
			end
		end

feature -- Modification (side effect)

	assigns (new: EBSET [G])
		local
			i: INTEGER
		do
			elements.wipe_out
			from
				i := 1
			until
				i > new.card
			loop
				add (new.set[i])
				i := i + 1
			end
		end

feature -- Model

	set: MML_SEQUENCE [G]
		do
			Result := elements.sequence
		end

feature -- Access

	is_user_type_def: BOOLEAN
			-- determines if an heir is a type defined by the user in Event-B (a carrier set)

feature {NONE} -- Access

	elements: V_LINKED_LIST [G]

feature {EBSET} -- wipeout
	wipe_out
		do
			elements.wipe_out
		end

feature -- Redefinition

	out: STRING
		do
			Result := "{"
			across
				elements as e
			loop
				if attached e.item as el then
					Result := Result + el.out
				end
				if e.target_index < elements.count then
					Result := Result + ", "
				end
			end
			Result := Result + "}"
		end

invariant
	no_repeated_elements: true

end
