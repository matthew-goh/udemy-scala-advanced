// call-by-value functions compute the passed-in expression's value before calling the function,
// thus the same value is accessed every time.

// Instead, call-by-name functions recompute the passed-in expression's value every time it is accessed

def byName(n: => Int): Int = n + 1
def byValue(n: Int): Int = n + 1
// Note: => Int is a different type from Int;
// the former is a "function of no arguments that will generate an Int"
// the latter is just Int
