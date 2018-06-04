note
	description: "Summary description for {EBREL_OPER}."
	author: ""
	date: "$Date$"
	revision: "$Revision$"

class
	EBREL_OPER [A, B, C, D]
--TODO check for ID
--		to put them all as creation procedures

feature -- Operations on Realtions
	forward (r: EBREL [A, B]; s: EBREL [B, C]) : EBREL [A, C]
			-- Forward Composition: An element x is related by 'r;s' to an element y
			--	if there is an element z such that 'r' relates x to z and 's' relates z to y.
		local
			i, j: INTEGER
			tmp: EBSET [C]
		do
			create Result.default_create (r.domain_type, s.range_type)
			-- check ID
			from
				i := 1
			until
				i > r.card
			loop
				from
					tmp := s.rel_image (create {EBSET [B]}.singleton (r.set [i].prj2))
					j := j + 1
				until
					j > tmp.card
				loop
					Result.add (create {EBPAIR [A, C]}.make (r.set [i].prj1, tmp.set[j]))
					j := j + 1
				end
				i := i + 1
			end
		end

	backward (r: EBREL [A, B]; s: EBREL [B, C]) : EBREL [C, A]
			-- Forward Composition: An element x is related by 'r;s' to an element y
			--	if there is an element z such that 'r' relates x to z and 's' relates z to y.
		do
			create Result.default_create (s.range_type, r.domain_type)
			Result.assigns (
				(create {EBREL_OPER [C, B, A, D]}).forward (s.inverse, r.inverse)
			)
		end

	override (r: EBREL [A, B]; s: EBREL [A, B]) : EBREL [A, B]
			-- 'r'<+'s' is equal to 'r' except all entries in 'r' whose first
			-- element is in the domain of 's' are replaced by the corresponding
			-- entries in 's'
		do
			create Result.default_create (r.domain_type, r.range_type)
			Result.assigns (s.union (r.domain_subtraction (s.dom)))
		end

	parallel (r: EBREL [A, B]; s: EBREL [C, D]) : EBREL [EBREL [A, C], EBREL[B, D]]
			-- The parallel product 'r'||'s' relates a pair x|->y to a pair m|->n when 'r'
			-- relates x to m and 's' relates y to n.
		local
			i, j: INTEGER
		do
			create Result.default_create (
								create {EBSET[EBREL [A, C]]}.singleton(create {EBREL [A,C]}.cartesian_prod (r.domain_type, s.domain_type)),
								create {EBSET [EBREL [B, D]]}.singleton(create {EBREL [B,D]}.cartesian_prod (r.range_type, s.range_type))
			)
			from
				i := 1
			until
				i > r.card
			loop
				from
					j := j + 1
				until
					j > s.card
				loop
					Result.add (create {EBPAIR [EBREL [A, C], EBREL [B, D]]}.make
								(create {EBREL [A, C]}.vals (
											<<create {EBPAIR[A, C]}.make (
														r.set[i].prj1, s.set[j].prj1)>>)
								,create {EBREL [B, D]}.vals (
											<<create {EBPAIR[B, D]}.make (
														r.set[i].prj2, s.set[j].prj2)>>)))
					j := j + 1
				end
				i := i + 1
			end
		end

	direct_product (r: EBREL [A, B]; s: EBREL [A, C]): EBREL [A, EBREL [B, C]]
			-- If a relation 'r' relates an element x to y and 's' relates x to z, the
			-- direct product 'r >< s' relates x to the pair y|->z
		local
			i, j: INTEGER
		do
			create Result.default_create (r.domain_type,
							create {EBSET[EBREL [B, C]]}.singleton(create {EBREL [B,C]}.cartesian_prod (r.range_type, s.range_type)))
			from
				i := 1
			until
				i > r.card
			loop
				from
					j := 1
				until
					j > s.card
				loop
					if attached r.set [i].prj1 as t and then
							attached s.set[j].prj1 as a and then t.is_equal (a) then
						Result.add (
							create {EBPAIR [A, EBREL [B, C]]}.make (r.set[i].prj1,
									create {EBREL [B, C]}.vals (
										<< create {EBPAIR [B, C]}.make (r.set[i].prj2,
																		s.set[j].prj2) >>
											)))
					end
					j := j + 1
				end
				i := i + 1
			end
		end
end
