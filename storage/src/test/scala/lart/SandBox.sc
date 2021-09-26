val a: PartialFunction[String, Int] = { case "1" =>
  1
}

val b: PartialFunction[String, String] = {
  case s if s.length > 2 => s * 2
}

def concat[In, T1, T2](p1: PartialFunction[In, T1],
                       p2: PartialFunction[In, T2]
): PartialFunction[In, T1 | T2] =
  p1 orElse p2

val c: PartialFunction[String, Int | String] = concat(a, b)

val d = c.lift

d("1")
d("aaa")
d("2")
