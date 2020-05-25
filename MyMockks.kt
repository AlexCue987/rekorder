import io.mockk.every
import io.mockk.mockk

class MyMockks {

fun getThisRover1(): IRover{
val thisRover1 = mockk<IRover>()
every{ thisRover1.explore(1) }.
returns(2)
return thisRover1
}

fun getThisRover2(): IRover{
val thisRover2 = mockk<IRover>()
every{ thisRover2.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover2
}

fun getThisRover1(): IRover{
val thisRover1 = mockk<IRover>()
every{ thisRover1.explore(1) }.
returns(2)
return thisRover1
}

fun getThisRover2(): IRover{
val thisRover2 = mockk<IRover>()
every{ thisRover2.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover2
}

fun getThisRover1(): IRover{
val thisRover1 = mockk<IRover>()
every{ thisRover1.explore(1) }.
returns(2)
return thisRover1
}

fun getThisRover2(): IRover{
val thisRover2 = mockk<IRover>()
every{ thisRover2.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover2
}

fun getThisRover1(): IRover{
val thisRover1 = mockk<IRover>()
every{ thisRover1.explore(1) }.
returns(2)
return thisRover1
}

fun getThisRover2(): IRover{
val thisRover2 = mockk<IRover>()
every{ thisRover2.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover2
}

fun getThisRover1(): IRover{
val thisRover1 = mockk<IRover>()
every{ thisRover1.explore(1) }.
returns(2)
return thisRover1
}

fun getThisRover2(): IRover{
val thisRover2 = mockk<IRover>()
every{ thisRover2.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover2
}

fun getThisRover1(): IRover{
val thisRover1 = mockk<IRover>()
every{ thisRover1.explore(1) }.
returns(2)
return thisRover1
}

fun getThisRover2(): IRover{
val thisRover2 = mockk<IRover>()
every{ thisRover2.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover2
}

fun getThisRover1(): IRover{
val thisRover1 = mockk<IRover>()
every{ thisRover1.explore(1) }.
returns(2)
return thisRover1
}

fun getThisRover2(): IRover{
val thisRover2 = mockk<IRover>()
every{ thisRover2.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover2
}
}