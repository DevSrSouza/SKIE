let a: A = AA2(k: 0)

switch onEnum(of: a) {
    case .Else:
        exit(1)
    case .A2(let a2):
        exit(a2.k)
}