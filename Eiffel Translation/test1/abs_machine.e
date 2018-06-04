class
	ABS_MACHINE

create
	initialisation

feature -- Initialisation
	initialisation
		do
			create persons.default_create
			create contents.default_create
			create owner.default_create
			create pages.default_create

		ensure
			persons.is_empty
			contents.is_empty
			owner.is_empty
			pages.is_empty
		end
feature -- Events
	create_account (p1: PERSON; c1: CONTENTS)
		require
			grd1: PERSON.difference (persons).has (p1)
			grd2: CONTENTS.difference (contents).has (c1)
		do
			contents.assigns ( (contents).union (create {EBSET[CONTENTS]}.singleton (c1)))
			persons.assigns ( (persons).union (create {EBSET[PERSON]}.singleton (p1)))
			owner.assigns ( (owner).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>)))
			pages.assigns ( (pages).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>)))
		end

	upload (p1: PERSON; c2: CONTENTS)
		require
			grd1: persons.has (p1)
			grd2: CONTENTS.difference (contents).has (c2)
		do
			contents.assigns ( (contents).union (create {EBSET[CONTENTS]}.singleton (c2)))
			owner.assigns ( (owner).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c2, p1))>>)))
			pages.assigns ( (pages).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c2, p1))>>)))
		end

	make_visible (p1: PERSON; c1: CONTENTS)
		require
			grd1: persons.has (p1)
			grd2: contents.has (c1)
			grd3: not pages.has ((create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1)))
		do
			pages.assigns ( (pages).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>)))
		end

	hide (p1: PERSON; c1: CONTENTS)
		require
			grd1: persons.has (p1)
			grd2: contents.has (c1)
			grd3: pages.has ((create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1)))
			grd4: owner.apply (c1) /= p1
		do
			pages.assigns ( pages.difference (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>)))
		end

	delete (cts: EBSET [CONTENTS]; ow: PERSON; c1: CONTENTS)
		require
			grd1: contents.has (c1)
			grd2: (create {EBSET[CONTENTS]}.singleton (c1)).union (cts).is_strict_subset ((owner.range_restriction (create {EBSET[PERSON]}.singleton (ow))).domain)(create {EBSET[CONTENTS]}.singleton (c1)).union (cts).is_subset ((owner.range_restriction (create {EBSET[PERSON]}.singleton (ow))).domain)
			grd3: cts.is_subset (contents)
			grd4: not cts.has (c1)
			grd5: owner.rel_image((create {EBSET[CONTENTS]}.singleton (c1)).union (cts)).is_equal (create {EBSET[PERSON]}.singleton (ow))
		do
			contents.assigns ( contents.difference ((create {EBSET[CONTENTS]}.singleton (c1)).union (cts)))
			owner.assigns ( owner.domain_subtraction ((create {EBSET[CONTENTS]}.singleton (c1)).union (cts)))
			pages.assigns ( pages.domain_subtraction ((create {EBSET[CONTENTS]}.singleton (c1)).union (cts)))
		end

	transmit_rc (prs: EBSET [PERSON]; rc: CONTENTS; ow: PERSON)
		require
			grd1: prs.is_subset (persons)
			grd2: contents.has (rc)
			grd3: owner.apply (rc).is_equal (ow)
			grd4: not prs.has (owner.apply (rc))
		do
			pages.assigns ( (pages).union ((create {EBREL [CONTENTS,EBSET [PERSON]]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (rc), prs)))
		end

	edit_owned (newc: CONTENTS; p1: PERSON; c1: CONTENTS)
		require
			grd1: contents.has (c1)
			grd2: persons.has (p1)
			grd3: owner.apply (c1).is_equal (p1)
			grd4: CONTENTS.difference (contents).has (newc)
		do
			contents.assigns ( (contents.difference (create {EBSET[CONTENTS]}.singleton (c1))).union (create {EBSET[CONTENTS]}.singleton (newc)))
			pages.assigns ( (pages.domain_subtraction (create {EBSET[CONTENTS]}.singleton (c1))).union ((create {EBREL [CONTENTS,CONTENTS]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (newc), pages.rel_image(create {EBSET[CONTENTS]}.singleton (c1)))))
			owner.assigns ( (owner.domain_subtraction (create {EBSET[CONTENTS]}.singleton (c1))).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (newc, p1))>>)))
		end

	edit_not_owned (newc: CONTENTS; p1: PERSON; c1: CONTENTS)
		require
			grd1: contents.has (c1)
			grd2: persons.has (p1)
			grd3: owner.apply (c1) /= p1
			grd4: CONTENTS.difference (contents).has (newc)
		do
			contents.assigns ( (contents).union (create {EBSET[CONTENTS]}.singleton (newc)))
			pages.assigns ( (pages.difference (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>))).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (newc, p1))>>)))
			owner.assigns ( (owner).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (newc, p1))>>)))
		end

	comment_rc (prs: EBSET [PERSON]; rc: CONTENTS; cmt: CONTENTS)
		require
			grd1: contents.has (rc)
			grd2: CONTENTS.difference (contents).has (cmt)
			grd3: prs.is_subset (persons)
			grd4: (create {EBREL [CONTENTS,EBSET [PERSON]]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (rc), prs).is_subset (pages)
			grd5: prs /=  create {EBSET}.empty_set
		do
			owner.assigns ( (owner).union (create {EBREL[CONTENTS,null]}.vals (<<(create {EBPAIR[CONTENTS,null]}.make (cmt, owner.apply (rc)))>>)))
			contents.assigns ( (contents).union (create {EBSET[CONTENTS]}.singleton (cmt)))
			pages.assigns ( (pages).union ((create {EBREL [EBSET [PERSON],CONTENTS]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (cmt), prs)).union (create {EBREL[CONTENTS,null]}.vals (<<(create {EBPAIR[CONTENTS,null]}.make (cmt, owner.apply (rc)))>>)))
		end

feature -- Access

		contents: EBSET [CONTENTS]
		owner: EBREL [CONTENTS, PERSON]
		pages: EBREL [CONTENTS, PERSON]
		persons: EBSET [PERSON]
invariant
		inv1: persons.is_subset (PERSON)
		inv2: contents.is_subset (CONTENTS)
		inv3: owner.is_tsurj_func
		inv4: pages.is_tsurj_rel
		inv5: owner.is_subset (pages)

end