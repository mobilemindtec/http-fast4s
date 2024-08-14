package br.com.mobilemind.beast

import scala.scalanative.unsafe.*

@extern
object beast_server:


    // name, value
    type BeastHeader = CStruct2[CString, CString]

    type BeastHeaderPtr = Ptr[BeastHeader]

    // header start pointer, size
    type BeastHeaders = CStruct2[BeastHeaderPtr, CInt]
    type BeastHeadersPtr = Ptr[BeastHeaders]

    // body {str, body raw, int}
    type BeastBody = CStruct3[CString, Ptr[Byte], CInt]
    type BeastBodyPtr = Ptr[BeastBody]

    // verb, target, content type, {body str, body bytes, size} , {[{name, value], size}
    type BeastRequest = CStruct5[CString, CString, CString, BeastBodyPtr, BeastHeadersPtr]
    type BeastRequestPtr = Ptr[BeastRequest]
    // status, headers, body, contentType

    // status {code, content type, {body str, body bytes, size}, {[{name, value], size}}
    type BeastResponse = CStruct4[CInt, CString, BeastBodyPtr, BeastHeadersPtr]
    type BeastResponsePtr = Ptr[BeastResponse]

    type BeastHandlerCallback = CFuncPtr2[BeastRequestPtr, BeastResponsePtr, Unit]
    type BeastHttpHandlerSync = CFuncPtr1[BeastRequestPtr, BeastResponsePtr]
    type BeastHttpHandlerAsync = CFuncPtr2[BeastRequestPtr, BeastHandlerCallback, Unit]

    type ThreadInit = CFuncPtr1[Ptr[Byte], Unit]
    type ThreadStarter =  CFuncPtr3[ThreadInit, CInt, Ptr[Byte], Unit]

    @name("run_sync")
    def runBeastSync(hostname: CString,
                     port: CUnsignedShort,
                     maxThread: CUnsignedShort,
                     threadStarter: ThreadStarter,
                     handler: BeastHttpHandlerSync): CInt = extern

    @name("run_async")
    def runBeastAsync(hostname: CString,
                      port: CUnsignedShort,
                      maxThread: CUnsignedShort,
                      threadStarter: ThreadStarter,
                      handler: CFuncPtr): CInt = extern


    @name("thread_test")
    def threadTest(starter: ThreadStarter): Unit = extern
