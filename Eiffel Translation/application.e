note
	description : "EB_Math_Lang application root class"
	date        : "$Date$"
	revision    : "$Revision$"

class
	APPLICATION

inherit
	ARGUMENTS

create
	make

feature {NONE} -- Initialization

	make
		local
			network: ABS_MACHINE
			bob : PERSONS
			marley: PERSONS
			b_c, m_c, up_ctn: CONTENTS
		do
			create network.initialisation
				-- create two accounts
			create bob.make ("BOB")
			create b_c.make ("bob_ctn")
			create marley.make ("MARLEY")
			create m_c.make ("mar_ctn")
			network.create_account (bob, b_c)
			network.create_account (marley, m_c)

				-- bob creates a content and uploads it
			create up_ctn.make ("up_ctn")
			network.upload (up_ctn, bob)

			

			print ("%N%N===DONE%N")
		end

	make11
		local
			a, b, c: EBSET[INTEGER]
			t : EBREL [INTEGER, INTEGER]
		do
			create a.singleton (1)
			a := a.union (create {EBSET[INTEGER]}.singleton (2))
			a := a.union (create {EBSET[INTEGER]}.singleton (3))

			create b.singleton (1)
			b := b.union (create {EBSET[INTEGER]}.singleton (2))
			b := b.union (create {EBSET[INTEGER]}.singleton (3))

			create c.singleton (2)
			c := c.union (create {EBSET[INTEGER]}.singleton (4))

			create t.default_create (a, b)


			print ("%N=========%N")
			io.new_line
			print ("t.domain_type:%N")
			print (t.domain_type)
			io.new_line
			print ("t.range_type:%N")
			print (t.range_type)
			--a.add (5)

			b := b.union (create {EBSET[INTEGER]}.singleton (6))
			io.new_line
			print ("t.domain_type:%N")
			print (t.domain_type)
			print ("%N=========%N")

			t := t.union (create
					{EBREL [INTEGER, INTEGER]}.vals (
					<< create {EBPAIR [INTEGER, INTEGER]}.make (1, 1)
					,
					--create {EBPAIR [INTEGER, INTEGER]}.make (1, 2),
					create {EBPAIR [INTEGER, INTEGER]}.make (3, 3),
					create {EBPAIR [INTEGER, INTEGER]}.make (2, 3)>>))
			io.new_line
			print (a)
			io.new_line
			print (b)
			io.new_line
			print (t)
			io.new_line
			print (t.dom)
			io.new_line
			print (t.ran)
			io.new_line
			print ("t.domain_type:%N")
			print (t.domain_type)
			io.new_line
			print ("t.range_type:%N")
			print (t.range_type)
			a := a.union (create {EBSET[INTEGER]}.singleton (5))
			b := b.union (create {EBSET[INTEGER]}.singleton (6))
			io.new_line
			print ("t.domain_type:%N")
			print (t.domain_type)
			io.new_line
			print ("t.range_type:%N")
			print (t.range_type)

			io.new_line
			print (t.domain_restriction (c))
			io.new_line
			print (t.is_function)
			io.new_line
			print (t.apply (2))
		end

	make2
		local
			persons : EBSET[PERSONS]
			p : PERSONS
		do
			create persons
			create p
			print (persons.is_strict_subset (create {PERSONS}))
			io.new_line
			print ((create {PERSONS}).has (p))
			print (1 |..| 10)
		end

end
