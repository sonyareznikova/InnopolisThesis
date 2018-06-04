note
	description: "[
		{EBPAIR} implements EventB pair E |-> F.
	]"
	author: "Victor Rivera"
	date: "$Date$"
	revision: "$Revision$"

class
	EBPAIR [E, F]

inherit
	ANY
		redefine
			out
		end

create
	make

feature -- Initialisation

	make (f_value: E; s_value: F)
			-- initialises the pair
		do
			prj1 := f_value
			prj2 := s_value
		end

feature -- Redefinition
	out: STRING
		do
			Result := "("
			if attached prj1 as v then
				Result := Result + v.out + ","
			else
				Result := Result + "ERROR,"
			end
			if attached prj2 as v then
				Result := Result + v.out
			else
				Result := Result + "ERROR"
			end
			Result := Result + ")"
		end

feature -- Access
	prj1: E
		-- 'prj1' is the first projection of the pair

	prj2: F
		-- 'prj2' is the second projection of the pair

invariant
	prj1 /= Void
	prj2 /= Void

end
