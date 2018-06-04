note
	description: "[
		{EBREL} implements EventB relations and functions: both of them are 
		sets of pairs. The additional constraints are imposed by queries.
		
		S <-> T is the set of relations between the two sets S and T. A relation
		consists of pairs where the first element is of S and the second of T.
	]"
	author: "Victor Rivera"
	date: "$Date$"
	revision: "$Revision$"

class
	EBREL [S, T]

inherit
	EBSET [EBPAIR[S,T]]
		rename
			default_create as default_create_set,
			from_set as from_rel
		export {NONE}
				singleton,
				default_create_set,
				min,
				max
		redefine
			union,
			add,
			from_rel,
			intersection,
			difference,
			assigns,
			is_equal,
			partition
			-- TODO pow and pow1
		end

create
	default_create,
	from_rel,
	cartesian_prod,
	vals

feature -- Initialisation
	default_create (dom_type : EBSET [S]; ran_type: EBSET [T])
			-- initialises the Current relation with dom and ran
		do
			create elements
			domain_type := dom_type
			range_type := ran_type
			create dom.default_create
			create ran.default_create
		end

	vals (v: ARRAY [EBPAIR [S, T]])
			-- initialises the relation with pairs in v
			-- domain_type and range_type are the union of pairs
		do
			create elements
			create dom
			create ran
			create domain_type
			create range_type
			across
				v as pairs
			loop
				add (pairs.item)
				domain_type.add (pairs.item.prj1)
				range_type.add (pairs.item.prj2)
			end
		end

	cartesian_prod (domain: EBSET [S]; range: EBSET [T])
			-- gives the set of pairs (relation) where the first element is a member of
			--	'd' and second element is a member of 'r'.
		local
			i, j: INTEGER
		do
			default_create (domain, range)
			from
				i := 1
			until
				i > domain.card
			loop
				from
					j := 1
				until
					j > range.card
				loop
					add (create {EBPAIR [S, T]}.make (domain.set[i], range.set[j]))
					j := j + 1
				end
				i := i + 1
			end
		end

feature{EBREL} -- Initialisation
	from_rel (other: EBREL [S, T])
			-- initialises the Current relation from another relation ('other')
		do
			Precursor (other)
			domain_type := other.domain_type.twin
			range_type := other.range_type.twin
			dom := other.dom.twin
			ran := other.ran.twin
		end

feature -- Queries on relation (different types of relations)
	is_total_rel: BOOLEAN
			-- is the Current relation a total relation (S <<-> T)
				-- each element of S relates to at least one element of T?
		do
			Result := dom.is_equal (domain_type)
		end

	is_surj_rel: BOOLEAN
			-- is the Current relation a surjective relation (S <->> T)
				--Is there at least one element of S for each element of T such that
				-- both are related?
		do
			Result := ran.is_equal (range_type)
		end

	is_tsurj_rel: BOOLEAN
			-- is the Current relation a total surjective relation (S <<->> T)
				-- Is the Current relation a total and a surjective relation?
		do
			Result := is_total_rel and then is_surj_rel
		end

feature -- Queries on functions (different types of functions) (a function is a relation with some constrains)	
	is_partial_func, is_function: BOOLEAN
			-- Is the Current relation a function?
			-- Is the Current relation a partial function?
			-- A partial function from S to T is a relation that maps an element of S to
			--	at most one element of T
		local
			i : INTEGER
		do
			from
				i := 1
			until
				i > dom.card or else
				rel_image (create {EBSET [S]}.singleton (dom.set [i])).card > 1
			loop
				i := i + 1
			end
			Result := i > dom.card
		ensure
			Result implies across 1 |..| card as e all rel_image (create {EBSET [S]}.singleton (dom.set [e.item])).card = 1 end
		end

	is_total_func: BOOLEAN
			-- Is the Current relation a total function?
			-- Does 'dom' contains all elements of S, i.e. it maps every element of
			--	S to an element of T.
		do
			Result := is_function and then dom.is_equal (domain_type)
		ensure
			Result implies is_function and then dom.is_equal (domain_type)
		end

	is_pinj_func: BOOLEAN
			-- Is the Current relation a partial injetive function?
		do
			Result := is_function and then inverse.is_function
		ensure
			Result implies is_function and then inverse.is_function
		end

	is_tinj_func: BOOLEAN
			-- Is the Current relation a total injetive function?
		do
			Result := is_total_func and then inverse.is_function
		ensure
			Result implies is_total_func and then inverse.is_function
		end

	is_psurj_func: BOOLEAN
			-- Is the Current relation a partial surjective function?
		do
			Result := is_function and then ran.is_equal (range_type)
		ensure
			Result implies is_function and then ran.is_equal (range_type)
		end

	is_tsurj_func: BOOLEAN
			-- Is the Current relation a total surjective function?
		do
			print ("is_tsurj_func: %N")
			print ("is_total_func: " + is_total_func.out + "%N")
			print ("the other: " + (ran.set = range_type.set).out + "%N" )
			print ("the other2: " + (ran.is_equal (range_type)).out + "%N" )
			print ("ran: " + ran.out + "%N")
			print ("range_type: " + range_type.out + "%N")
			Result := is_total_func and then ran.is_equal (range_type)
		ensure
			Result implies is_total_func and then ran.is_equal (range_type)
		end

	is_biject_func: BOOLEAN
			-- Is the Current relation a bijective function?
		do
			Result := is_total_func and then is_tsurj_rel
		ensure
			Result implies is_total_func and then is_tsurj_rel
		end

feature -- Modification (noside-effect)

	domain_restriction (set_res: EBSET [S]): EBREL [S, T]
			-- gives a subset of the relation Current that contains all
			-- of the pairs whose first element is in 'set_res'
		do
			create Result.default_create (domain_type.twin, range_type.twin)
			across
				elements as e
			loop
				if set_res.has (e.item.prj1) then
					Result.add (e.item)
				end
			end
		ensure
			Result.is_subset (Current)
			across 1 |..| Result.card as e all set_res.has (Result.set [e.item].prj1) end
		end

	domain_subtraction (set_sub: EBSET [S]): EBREL [S, T]
			-- gives a subset of the Current relation that contains all
			-- of the pairs whose first elements is not in 'set_sub'.
		do
			create Result.default_create (domain_type.twin, range_type.twin)
			across
				elements as e
			loop
				if not set_sub.has (e.item.prj1) then
					Result.add (e.item)
				end
			end
		ensure
			Result.is_subset (Current)
			across 1 |..| Result.card as e all not set_sub.has (Result.set [e.item].prj1) end
		end

	range_restriction (set_res: EBSET [T]): EBREL [S, T]
			-- gives a subset of the Current relation that contains all
			-- of the pairs whose second element is in 'set_res'
		do
			create Result.default_create (domain_type.twin, range_type.twin)
			across
				elements as e
			loop
				if set_res.has (e.item.prj2) then
					Result.add (e.item)
				end
			end
		ensure
			Result.is_subset (Current)
			across 1 |..| Result.card as e all set_res.has (Result.set [e.item].prj2) end
		end

	range_subtraction (set_sub: EBSET [T]): EBREL [S, T]
			-- gives a subset of the Current relation that contains all
			-- of the pairs whose second element is not in 'set_sub'.
		do
			create Result.default_create (domain_type.twin, range_type.twin)
			across
				elements as e
			loop
				if not set_sub.has (e.item.prj2) then
					Result.add (e.item)
				end
			end
		ensure
			Result.is_subset (Current)
			across 1 |..| Result.card as e all not set_sub.has (Result.set [e.item].prj2) end
		end

	-- TODO: Relational forward composition	
		  -- Relational backward composition
		  -- Relational override
		  -- Parallel product
		  -- Direct product

	converse, inverse : EBREL [T, S]
			-- relates an element x to y if the Current relation relates y to x.
		do
			create Result.default_create (range_type.twin, domain_type.twin)
			across
				elements as e
			loop
				Result.add (create {EBPAIR [T, S]}.make (e.item.prj2, e.item.prj1))
			end
		ensure
			across 1 |..| Result.card as e all has (create {EBPAIR [S, T]}.make (Result.set [e.item].prj2, Result.set [e.item].prj1)) end
		end

	rel_image (set_ima: EBSET[S]): EBSET[T]
			-- gives those elements in the range of Current relation that are mapped from 'set_ima'.
		do
			create Result.default_create
			across
				elements as e
			loop
				if set_ima.has (e.item.prj1) then
					Result.add (e.item.prj2)
				end
			end
		ensure
			-- TODO {y | \exists x . x : set_ima and (x,y) : Current}
		end

	--TODO ID

	apply (v: S) : T
			-- yields the value that maps 'v'
		require
			v_in_dom: dom.has (v)
			current_is_a_function: is_function
		local
			i : INTEGER
		do
			from
				i := 1
			until
				i > elements.count or else
				(attached elements.item (i).prj1 as t and then
					attached v as a and then t.is_equal (a))
			loop
				i := i + 1
			end
			Result := elements.item (i).prj2
		end

	--TODO: Lambda (it can be defined using agents)

--	forward (other: EBREL_OPER [S, ANY]): EBREL [S, ANY]
--		do
--			
--		end

feature {EBREL, EBREL_OPER} -- Redefinition

	add (v: EBPAIR[S,T])
			-- FROM EBSET
		do
			Precursor (v)
			dom.add (v.prj1)
			ran.add (v.prj2)
		end

feature -- Redefinition

	union (other: EBREL [S, T]): EBREL [S, T]
			-- FROM EBSET
		local
			i : INTEGER
		do
			create Result.from_rel (Current)

			--Result.update_type_dom (new_dom_type (domain_type, other.domain_type),
			--						new_ran_type (range_type, other.range_type))

			from
				i := 1
			until
				i > other.card
			loop
				Result.add (other.set [i])
				i := i + 1
			end
		end

	intersection (other: EBREL [S, T]): EBREL [S, T]
			-- FROM EBSET
		do
			--create Result.default_create (new_dom_type (domain_type, other.domain_type),
			--								new_ran_type (range_type, other.range_type))
			create Result.default_create (domain_type.twin, range_type.twin)
			across
				elements as e
			loop
				if other.has (e.item) then
					Result.add (e.item)
				end
			end
		end

	difference (other: EBREL [S, T]): EBREL [S, T]
			-- FROM EBSET
		do
			--create Result.default_create (new_dom_type (domain_type, other.domain_type),
			--								new_ran_type (range_type, other.range_type))
			create Result.default_create (domain_type.twin, range_type.twin)
			across
				elements as e
			loop
				if not other.has (e.item) then
					Result.add (e.item)
				end
			end
		end

	is_equal (other: EBREL [S, T]) : BOOLEAN
		do
			Result := Precursor (other)
		end

	partition (sets: ARRAY [EBREL [S, T]]): BOOLEAN
			-- FROM EBSET
		do
			Result := Precursor (sets)
		end

feature {EBREL} -- Modification
	update_type_dom (new_type_dom: EBSET [S]; new_type_ran: EBSET [T])
			-- updates the type dom and ran of Current
		do
			domain_type := new_type_dom.twin
			range_type := new_type_ran.twin
		end

feature {NONE} -- Helpers
	new_dom_type (domain_type1, domain_type2: EBSET [S]): EBSET [S]
		do
			if domain_type1.finite and then domain_type2.finite then
				create Result.from_set (domain_type1.union (domain_type2))
			elseif not domain_type1.finite then
				Result := domain_type1.twin
			else
				Result := domain_type2.twin
			end
		end

	new_ran_type (ran_type1, ran_type2: EBSET [T]): EBSET [T]
		do
			if ran_type1.finite and then ran_type2.finite then
				create Result.from_set (ran_type1.union (ran_type2))
			elseif not ran_type1.finite then
				Result := ran_type1.twin
			else
				Result := ran_type2.twin
			end
		end

feature -- Redefinition
	assigns (new: EBREL [S, T])
			-- implements assignment from EventB.
			-- the idea is not to lose the corresponding aliases
		local
			i: INTEGER
		do
			elements.wipe_out
			dom.wipe_out
			ran.wipe_out
			from
				i := 1
			until
				i > new.card
			loop
				add (new.set [i])
				dom.add (new.set [i].prj1)
				ran.add (new.set [i].prj2)
				i := i + 1
			end
		end

feature -- Access
	domain_type : EBSET [S]
		-- Contains all possible values in the domain

	range_type : EBSET [T]
		-- Contains all possible values in the range

	dom: EBSET [S]
		-- contains the actual values of the domain
		-- 'dom' is the set of the elements of S that are related to at least one element of T by Current

	ran: EBSET [T]
		-- contains the actual values of the range
		-- 'ran' is the set of elements of T to which at least one element of S relates by Current.
invariant
	actual_vals_dom_correct: dom.set.range.is_subset_of (domain_type.set.range)
	actual_vals_ran_correct: ran.set.range.is_subset_of (range_type.set.range)
end
