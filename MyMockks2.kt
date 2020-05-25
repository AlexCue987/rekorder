import io.mockk.every
import io.mockk.mockk

class MyMockks2 {

fun getThisRover(): IRover{
val thisRover = mockk<IRover>()
every{ thisRover.explore(1) }.
returns(2)

every{ thisRover.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover
}

fun getThisRover(): IRover{
val thisRover = mockk<IRover>()
every{ thisRover.explore(1) }.
returns(2)

every{ thisRover.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover
}

fun getThisRover(): IRover{
val thisRover = mockk<IRover>()
every{ thisRover.explore(1) }.
returns(2)

every{ thisRover.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover
}

fun getThisRover(): IRover{
val thisRover = mockk<IRover>()
every{ thisRover.explore(1) }.
returns(2)

every{ thisRover.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover
}

fun getThisRover(): IRover{
val thisRover = mockk<IRover>()
every{ thisRover.explore(1) }.
returns(2)

every{ thisRover.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover
}

fun getThisRover(): IRover{
val thisRover = mockk<IRover>()
every{ thisRover.explore(1) }.
returns(2)

every{ thisRover.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover
}

fun getThisRover(): IRover{
val thisRover = mockk<IRover>()
every{ thisRover.explore(1) }.
returns(2)

every{ thisRover.add(BigDecimal("1"),
LocalDate.of(-999999999, 1, 1),
"Any",
1) }.
returns(2)
return thisRover
}
}