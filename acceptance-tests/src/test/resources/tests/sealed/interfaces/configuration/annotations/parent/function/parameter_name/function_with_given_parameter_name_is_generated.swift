let a: A = A1()

switch onEnum(a: a) {
    case .A1(_):
        exit(0)
    case .A2(_):
        exit(1)
}