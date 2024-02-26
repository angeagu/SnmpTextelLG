//	Aaeon Watchdog SDK header file
//	Copyright(C)Aaeon Technology Inc., 2006
//	Unauthorized access prohibit
//	Written by Samuel Lin
//
//	aonWdt.h
//

#ifndef __AONWDT_H__
#define __AONWDT_H__

#ifdef __cplusplus
extern "C"
{
#endif //__cplusplus



#ifdef AAEON_EXPORTS
#define DLLAPI __declspec(dllexport)
#else
#define DLLAPI __declspec(dllimport)
#endif

//	Open Watchdog Instance
//
//	Input:	reserved		//reserved must be zero
//
//	Return: Nonzero, the instance handle, if success, 
//			NULL if failed
//
//			Using GetLastError() to get error codes
//			Possible error codes are 
//				ERROR_SERVICE_LOGON_FAILED
//				ERROR_FILE_NOT_FOUND
//				ERROR_IO_PENDING
//	
DLLAPI HANDLE aaeonWdtOpen(
	DWORD reserved		//reserved must be zero
);

//	Close Watchdog Instance
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		TRUE if success, FALSE if failed
//
DLLAPI BOOL aaeonWdtClose(
	HANDLE hInst
);

//	Get Device ID
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		device id, LOWORD is the Major ID, HIWORD is the Subid.
//
DLLAPI WORD aaeonWdtGetDevID(
	HANDLE hInst
);

//	Get Device Version
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		device version, LOWORD is the major version, HIWORD is the minor version
//
DLLAPI WORD aaeonWdtGetDevVer(
	HANDLE hInst
);

//	Get Device Name
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		Device name in zero-terminated string format
//
DLLAPI LPCTSTR aaeonWdtGetDevName(
	HANDLE hInst
);

//	Set Watchdog Enable
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//		bEnable: TRUE if Enable, FALSE if Disable
//
//	Return:
//		S_OK if success, Otherwise failed
//
DLLAPI HRESULT aaeonWdtSetEnable(
	HANDLE hInst, 
	BOOL bEnable
);

//	Get Watchdog Enable
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//		*pbEnable: TRUE if Enable, FALSE if Disable
//
DLLAPI HRESULT aaeonWdtGetEnable(
	HANDLE hInst, 
	BOOL* pbEnable
);

DLLAPI HRESULT aaeonWdtSetPWRGDEnable(
	HANDLE hInst, 
	BOOL bEnable
);
DLLAPI HRESULT aaeonWdtGetPWRGDEnable(
	HANDLE hInst, 
	BOOL* pbEnable
);

//	Set Count Mode
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//		bMinute: TRUE if Minute mode, FALSE if second mode
//
//	Return:
//		S_OK if success, Otherwise failed
//
DLLAPI HRESULT aaeonWdtSetCountMode(
	HANDLE hInst, 
	BOOL bMinute
);

//	Get Count mode
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//		*pbMinute: TRUE if Minute mode, FALSE if second mode
//
DLLAPI HRESULT aaeonWdtGetCountMode(
	HANDLE hInst, 
	BOOL* pbMinute
);

//	Set Timeout Count
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//		tTimeout: set the counter
//				(the Count Mode (unit) is defined by aaeonWdtSetCountMode)
//
//	Return:
//		S_OK if success, Otherwise failed
//
DLLAPI HRESULT aaeonWdtSetTimeoutCount(
	HANDLE hInst, 
	DWORD tTimeout
);

//	Get Timeout count
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//		*ptTimeout: the timeout count
//
DLLAPI HRESULT aaeonWdtGetTimeoutCount(
	HANDLE hInst, 
	DWORD* ptTimeout
);

//	Get Instant Timeout count
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//		*ptTimeout: the Instant timeout count
//
DLLAPI HRESULT aaeonWdtGetCurTimeoutCount(
	HANDLE hInst, 
	DWORD* ptTimeout
);

//	Set Reset Enable according to the interrupt of mouse (PS2 mode)
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//		bEnable: TRUE if Enable, FALSE if Disable
//
//	Return:
//		S_OK if success, Otherwise failed
//
DLLAPI HRESULT aaeonWdtSetMouseResetEnable(
	HANDLE hInst, 
	BOOL bEnable
);

//	Get Reset Enable according to the interrupt of mouse (PS2 mode)
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//		*pbEnable: TRUE if Enable, FALSE if Disable.
//
DLLAPI HRESULT aaeonWdtGetMouseResetEnable(
	HANDLE hInst, 
	BOOL* pbEnable
);

//	Set Reset Enable according to the interrupt of Keyboard (PS2 mode)
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//		bEnable: TRUE if Enable, FALSE if Disable
//
//	Return:
//		S_OK if success, Otherwise failed
//
DLLAPI HRESULT aaeonWdtSetKeyboardResetEnable(
	HANDLE hInst, 
	BOOL bEnable
);

//	Get Reset Enable according to the interrupt of Keyboard (PS2 mode)
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//		*pbEnable: TRUE if Enable, FALSE if Disable
//
DLLAPI HRESULT aaeonWdtGetKeyboardResetEnable(
	HANDLE hInst, 
	BOOL* pbEnable
);

//	Set Timeout status
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//		bTimeout: TRUE if Timeout, FALSE if Down counting
//
//	Return:
//		S_OK if success, Otherwise failed
//
DLLAPI HRESULT aaeonWdtSetTimeoutStatus(
	HANDLE hInst, 
	BOOL bTimeout
);

//	Get Timeout status
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//		*pbTimeout: TRUE if Timeout, FALSE if Down counting
//
DLLAPI HRESULT aaeonWdtGetTimeoutStatus(
	HANDLE hInst, 
	BOOL* pbTimeout
);

//	Handshake with Watchdog to reload the Timeout count into the down counter.
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//
DLLAPI HRESULT aaeonWdtHandshake(
	HANDLE hInst
);

//	Force watchdog timeout immediately
//
//	Input:
//		hInst: A handle opened by aaeonWdtOpen
//
//	Return:
//		S_OK if success, Otherwise failed
//
DLLAPI HRESULT aaeonWdtForceTimeout(
	HANDLE hInst
);

#ifdef __cplusplus
}
#endif //__cplusplus

#endif //__AONWDT_H__
