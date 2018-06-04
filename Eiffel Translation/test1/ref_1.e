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
			create viewp.default_create
			create editp.default_create

		ensure
			persons.is_empty
			contents.is_empty
			owner.is_empty
			pages.is_empty
			viewp.is_empty
			editp.is_empty
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
			viewp.assigns ( (viewp).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>)))
			editp.assigns ( (editp).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>)))
		end

	upload (p1: PERSON; c2: CONTENTS)
		require
			grd1: persons.has (p1)
			grd2: CONTENTS.difference (contents).has (c2)
		do
			contents.assigns ( (contents).union (create {EBSET[CONTENTS]}.singleton (c2)))
			owner.assigns ( (owner).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c2, p1))>>)))
			pages.assigns ( (pages).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c2, p1))>>)))
			viewp.assigns ( (viewp).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c2, p1))>>)))
			editp.assigns ( (editp).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c2, p1))>>)))
		end

	make_visible (p1: PERSON; c1: CONTENTS)
		require
			grd1: persons.has (p1)
			grd2: contents.has (c1)
			grd3: not pages.has ((create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1)))
			grd4: viewp.has ((create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1)))
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
			viewp.assigns ( viewp.difference (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>)))
			editp.assigns ( editp.difference (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>)))
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
			viewp.assigns ( viewp.domain_subtraction ((create {EBSET[CONTENTS]}.singleton (c1)).union (cts)))
			editp.assigns ( editp.domain_subtraction ((create {EBSET[CONTENTS]}.singleton (c1)).union (cts)))
		end

	transmit_rc (prs: EBSET [PERSON]; rc: CONTENTS; ow: PERSON)
		require
			grd1: prs.is_subset (persons)
			grd2: contents.has (rc)
			grd3: owner.apply (rc).is_equal (ow)
			grd4: not prs.has (owner.apply (rc))
		do
			pages.assigns ( (pages).union ((create {EBREL [CONTENTS,EBSET [PERSON]]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (rc), prs)))
			viewp.assigns ( (viewp).union ((create {EBREL [CONTENTS,EBSET [PERSON]]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (rc), prs)))
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
			viewp.assigns ( (viewp).union ((create {EBREL [EBSET [PERSON],CONTENTS]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (cmt), prs)).union (create {EBREL[CONTENTS,null]}.vals (<<(create {EBPAIR[CONTENTS,null]}.make (cmt, owner.apply (rc)))>>)))
			editp.assigns ( (editp).union ((create {EBREL [EBSET [PERSON],CONTENTS]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (cmt), prs)).union (create {EBREL[CONTENTS,null]}.vals (<<(create {EBPAIR[CONTENTS,null]}.make (cmt, owner.apply (rc)))>>)))
		end

	grant_view_perm (p1: PERSON; rc: CONTENTS; ow: PERSON)
		require
			grd1: persons.has (p1)
			grd2: contents.has (rc)
			grd3: not viewp.has ((create {EBPAIR[CONTENTS,PERSON]}.make (rc, p1)))
			grd4: owner.apply (rc).is_equal (ow)
		do
			viewp.assigns ( (viewp).union (create {EBREL[PERSON,CONTENTS]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (rc, p1))>>)))
		end

	grant_edit_perm (p1: PERSON; rc: CONTENTS; ow: PERSON)
		require
			grd1: persons.has (p1)
			grd2: contents.has (rc)
			grd3: not editp.has ((create {EBPAIR[CONTENTS,PERSON]}.make (rc, p1)))
			grd4: viewp.has ((create {EBPAIR[CONTENTS,PERSON]}.make (rc, p1)))
			grd5: owner.apply (rc).is_equal (ow)
		do
			editp.assigns ( (editp).union (create {EBREL[PERSON,CONTENTS]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (rc, p1))>>)))
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
			viewp.assigns ( (viewp.domain_subtraction (create {EBSET[CONTENTS]}.singleton (c1))).union ((create {EBREL [CONTENTS,CONTENTS]}).cartesian_prod (create {EBSET[CONTENTS]}.singleton (newc), viewp.rel_image(create {EBSET[CONTENTS]}.singleton (c1)))))
			editp.assigns ( (editp.domain_subtraction (create {EBSET[CONTENTS]}.singleton (c1))).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (newc, p1))>>)))
		end

	edit_not_owned (newc: CONTENTS; p1: PERSON; c1: CONTENTS)
		require
			grd1: contents.has (c1)
			grd2: persons.has (p1)
			grd3: owner.apply (c1) /= p1
			grd4: CONTENTS.difference (contents).has (newc)
			grd5: editp.has ((create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1)))
		do
			contents.assigns ( (contents).union (create {EBSET[CONTENTS]}.singleton (newc)))
			pages.assigns ( (pages.difference (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>))).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (newc, p1))>>)))
			owner.assigns ( (owner).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (newc, p1))>>)))
			viewp.assigns ( (viewp.difference (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>))).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (newc, p1))>>)))
			editp.assigns ( (editp.difference (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (c1, p1))>>))).union (create {EBREL[CONTENTS,PERSON]}.vals (<<(create {EBPAIR[CONTENTS,PERSON]}.make (newc, p1))>>)))
		end

feature -- Access

		contents: EBSET [CONTENTS]
		editp: EBREL [CONTENTS, PERSON]
		owner: EBREL [CONTENTS, PERSON]
		pages: EBREL [CONTENTS, PERSON]
		persons: EBSET [PERSON]
		viewp: EBREL [CONTENTS, PERSON]
invariant
		inv1: persons.is_subset (PERSON)
		inv2: contents.is_subset (CONTENTS)
		inv3: owner.is_tsurj_func
		inv4: pages.is_tsurj_rel
		inv5: owner.is_subset (pages)
		inv6: contents rel persons.has (viewp)
		inv7: contents rel persons.has (editp)
		inv8: owner.is_subset (viewp)
		inv9: owner.is_subset (editp)
		inv10: editp.is_subset (viewp)
		inv11: pages.is_subset (viewp)

end