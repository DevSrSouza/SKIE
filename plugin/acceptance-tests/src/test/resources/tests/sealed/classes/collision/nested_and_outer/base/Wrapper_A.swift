let a: Wrapper.A = Wrapper.AA1()

switch onEnum(of: a) {
    case .A1(_):
        exit(0)
    case .A2(_):
        exit(1)
}