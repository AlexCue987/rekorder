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
                try {
                    val ret: Any? = method!!.invoke(instance, *(args ?: arrayOfNulls<Any>(0)))
                    val result = if (ret == null) UnitResult() else ObjectResult(ret!!)
                    callRecorder.save(FunctionCall(method.name, args.toList(), result))
                    ret
                } catch (ex: InvocationTargetException) {
                    val targetException = ex.targetException
                    val cause = targetException.cause
                    callRecorder.save(FunctionCall(method.name, args.toList(), ExceptionResult(targetException!!)))
                    throw targetException
                }
            } as T
            return proxy
        }

        @JvmStatic
        inline fun<reified T> getRecordingProxy(instance: Any): RecordingProxy<T> {
            val callRecorder = CallRecorder()
            val proxy = Proxy.newProxyInstance(
                T::class.java.classLoader,
                arrayOf(T::class.java)) { proxy, method, args ->
                try {
                    val ret: Any? = method!!.invoke(instance, *(args ?: arrayOfNulls<Any>(0)))
                    val result = if (ret == null) UnitResult() else ObjectResult(ret!!)
                    callRecorder.save(FunctionCall(method.name, args.toList(), result))
                    ret
                } catch (ex: InvocationTargetException) {
                    val targetException = ex.targetException
                    val cause = targetException.cause
                    callRecorder.save(FunctionCall(method.name, args.toList(), ExceptionResult(targetException!!)))
                    throw targetException
                }
            } as T
            return RecordingProxy(proxy, callRecorder, T::class.java.typeName)
        }
    }
}

data class RecordingProxy<T>(val proxy: T, val recorder: CallRecorder, val typeName: String)
