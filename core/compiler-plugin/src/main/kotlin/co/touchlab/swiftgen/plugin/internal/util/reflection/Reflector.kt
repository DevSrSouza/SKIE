package co.touchlab.swiftgen.plugin.internal.util.reflection

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal abstract class Reflector {

    private val reflectedClass: Class<*>

    protected abstract val instance: Any

    constructor(reflectedClass: Class<*>) {
        this.reflectedClass = reflectedClass
    }

    constructor(reflectedClass: KClass<*>) : this(reflectedClass.java)

    constructor(fqName: String) {
        reflectedClass = this::class.java.classLoader.loadClass(fqName)
    }

    protected inline fun <reified R> declaredMethod(): Provider<DeclaredMethod0<R>> =
        Provider { DeclaredMethod0(it, R::class.java) }

    protected inline fun <reified P1, reified R> declaredMethod(param1: Class<*>): Provider<DeclaredMethod1<P1, R>> =
        Provider { DeclaredMethod1(it, P1::class.java, R::class.java) }

    protected inline fun <reified T> declaredProperty(): Provider<DeclaredProperty<T>> =
        Provider { DeclaredProperty(it, T::class.java) }

    protected inline fun <reified T> declaredField(): Provider<DeclaredField<T>> =
        Provider { DeclaredField(it, T::class.java) }

    protected inline fun <reified T> extensionProperty(extensionClassFqName: String): Provider<ExtensionProperty<T>> =
        Provider { ExtensionProperty(it, extensionClassFqName, T::class.java) }

    protected class Provider<T>(private val factory: (String) -> T) : PropertyDelegateProvider<Reflector, T> {

        override fun provideDelegate(thisRef: Reflector, property: KProperty<*>): T = factory(property.name)
    }

    protected abstract inner class DeclaredMethod<T, R>(
        name: String,
        private val returnType: Class<R>,
        parameterTypes: Array<Class<*>>,
    ) : ReadOnlyProperty<Reflector, T> {

        private val method by lazy {
            reflectedClass.getDeclaredMethod(name, *parameterTypes).also { it.isAccessible = true }
        }

        protected fun invoke(arguments: Array<Any?>): R =
            method.invoke(instance, *arguments).let { returnType.cast(it) }
    }

    protected inner class DeclaredMethod0<R>(
        name: String,
        returnType: Class<R>,
    ) : DeclaredMethod<() -> R, R>(name, returnType, emptyArray()) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): () -> R = {
            invoke(emptyArray())
        }
    }

    protected inner class DeclaredMethod1<P1, R>(
        name: String,
        param1: Class<P1>,
        returnType: Class<R>,
    ) : DeclaredMethod<(P1) -> R, R>(name, returnType, arrayOf(param1)) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): (P1) -> R = {
            invoke(arrayOf(it))
        }
    }

    protected inner class DeclaredProperty<T>(name: String, type: Class<T>) : ReadWriteProperty<Reflector, T> {

        private val getter by lazy {
            DeclaredMethod0("get" + name.replaceFirstChar { it.uppercase() }, type)
        }
        private val setter by lazy {
            DeclaredMethod1("set" + name.replaceFirstChar { it.uppercase() }, type, Unit::class.java)
        }

        override fun getValue(thisRef: Reflector, property: KProperty<*>): T =
            getter.getValue(thisRef, property).invoke()

        override fun setValue(thisRef: Reflector, property: KProperty<*>, value: T) {
            setter.getValue(thisRef, property).invoke(value)
        }
    }

    protected inner class DeclaredField<T>(name: String, private val type: Class<T>) : ReadWriteProperty<Reflector, T> {

        private val field by lazy {
            reflectedClass.getDeclaredField(name).also { it.isAccessible = true }
        }

        override fun getValue(thisRef: Reflector, property: KProperty<*>): T =
            field.get(instance).let { type.cast(it) }

        override fun setValue(thisRef: Reflector, property: KProperty<*>, value: T) {
            field.set(instance, value)
        }
    }

    protected abstract inner class ExtensionMethod<T, R>(
        name: String,
        extensionClassFqName: String,
        private val returnType: Class<R>,
        parameterTypes: Array<Class<*>>,
    ) : ReadOnlyProperty<Reflector, T> {

        private val method by lazy {
            val extensionClass = this::class.java.classLoader.loadClass(extensionClassFqName)

            extensionClass.getDeclaredMethod(name, reflectedClass, *parameterTypes).also { it.isAccessible = true }
        }

        protected fun invoke(arguments: Array<Any?>): R =
            method.invoke(null, *arguments).let { returnType.cast(it) }
    }

    protected inner class ExtensionMethod0<R>(
        name: String,
        extensionClassFqName: String,
        returnType: Class<R>,
    ) : ExtensionMethod<() -> R, R>(name, extensionClassFqName, returnType, emptyArray()) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): () -> R = {
            invoke(arrayOf(instance))
        }
    }

    protected inner class ExtensionMethod1<P1, R>(
        name: String,
        extensionClassFqName: String,
        param1: Class<P1>,
        returnType: Class<R>,
    ) : ExtensionMethod<(P1) -> R, R>(name, extensionClassFqName, returnType, arrayOf(param1)) {

        override fun getValue(thisRef: Reflector, property: KProperty<*>): (P1) -> R = {
            invoke(arrayOf(instance, it))
        }
    }

    protected inner class ExtensionProperty<T>(
        name: String,
        extensionClassFqName: String,
        type: Class<T>,
    ) : ReadWriteProperty<Reflector, T> {

        private val getter by lazy {
            ExtensionMethod0("get" + name.replaceFirstChar { it.uppercase() }, extensionClassFqName, type)
        }
        private val setter by lazy {
            ExtensionMethod1("set" + name.replaceFirstChar { it.uppercase() }, extensionClassFqName, type, Unit::class.java)
        }

        override fun getValue(thisRef: Reflector, property: KProperty<*>): T =
            getter.getValue(thisRef, property).invoke()

        override fun setValue(thisRef: Reflector, property: KProperty<*>, value: T) {
            setter.getValue(thisRef, property).invoke(value)
        }
    }
}

