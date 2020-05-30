package org.kollektions.proksy.recorder

import org.kollektions.proksy.model.ExceptionResult
import org.kollektions.proksy.model.ObjectResult
import org.kollektions.proksy.model.UnitResult
import org.kollektions.proksy.model.FunctionCall
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy

class CallRecorder {
    private val functionCalls = mutableListOf<FunctionCall>()

    fun save(functionCall: FunctionCall) = functionCalls.add(functionCall)

    fun getCalls() = functionCalls.toList()

    fun flush(): List<FunctionCall> {
        val ret = functionCalls.toList()
        functionCalls.clear()
        return ret
    }

    companion object {
        @JvmStatic
        inline fun<reified T> getProxy(instance: Any, callRecorder: CallRecorder): T {
            val proxy = Proxy.newProxyInstance(
                T::class.java.classLoader,
                arrayOf(T::class.java)) { proxy, method, args ->
                val arguments = args?.toList() ?: listOf()
                try {
//                    val names = method.parameters.asSequence().map { it.name }.toList()
//                    print("Parameter names = $names")
                    val ret: Any? = method!!.invoke(instance, *(args ?: arrayOfNulls<Any>(0)))
                    val result = if (ret == null) UnitResult() else ObjectResult(ret!!)
                    callRecorder.save(FunctionCall(method.name, arguments, result))
                    ret
                } catch (ex: InvocationTargetException) {
                    val targetException = ex.targetException
                    val cause = targetException.cause
                    callRecorder.save(FunctionCall(method.name, arguments, ExceptionResult(targetException!!)))
                    throw targetException
                }
            } as T
            return proxy
        }

        @JvmStatic
        inline fun<reified T> getRecordingProxy(instance: Any): RecordingProxy<T> {
            val callRecorder = CallRecorder()
            val proxy = getProxy<T>(instance, callRecorder)
            return RecordingProxy(proxy, callRecorder, T::class.java.typeName)
        }
    }
}

data class RecordingProxy<T>(val proxy: T, val recorder: CallRecorder, val typeName: String)
