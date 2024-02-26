// WatchDog.cpp : Defines the class behaviors for the application.
//

#include "stdafx.h"
#include "WatchDog.h"
#include "WatchDogDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CWatchDogApp

BEGIN_MESSAGE_MAP(CWatchDogApp, CWinApp)
	ON_COMMAND(ID_HELP, &CWinApp::OnHelp)
END_MESSAGE_MAP()


// CWatchDogApp construction

CWatchDogApp::CWatchDogApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CWatchDogApp object

CWatchDogApp theApp;


// CWatchDogApp initialization

BOOL CWatchDogApp::InitInstance()
{
	// InitCommonControlsEx() is required on Windows XP if an application
	// manifest specifies use of ComCtl32.dll version 6 or later to enable
	// visual styles.  Otherwise, any window creation will fail.
	INITCOMMONCONTROLSEX InitCtrls;
	InitCtrls.dwSize = sizeof(InitCtrls);
	// Set this to include all the common control classes you want to use
	// in your application.
	InitCtrls.dwICC = ICC_WIN95_CLASSES;
	InitCommonControlsEx(&InitCtrls);

	CWinApp::InitInstance();

	AfxEnableControlContainer();

	// Standard initialization
	// If you are not using these features and wish to reduce the size
	// of your final executable, you should remove from the following
	// the specific initialization routines you do not need
	// Change the registry key under which our settings are stored
	// TODO: You should modify this string to be something appropriate
	// such as the name of your company or organization
	SetRegistryKey(_T("Local AppWizard-Generated Applications"));

	CWatchDogDlg dlg;
	m_pMainWnd = &dlg;
	//INT_PTR nResponse = dlg.DoModal();

	int timer = 10;
	int timeoutCount = timer + 5;


	
	//std::ofstream myFile;
	//myFile.open("C:\\Users\\a\\Desktop\\WatchDog\\release\\a.txt");

	HANDLE event_handle = CreateEvent(NULL, FALSE, FALSE, NULL);

	while(TRUE){
	//for(int i = 0; i < 10; i++){
		switch(WaitForSingleObject(event_handle, (timer * 1000))){
			case WAIT_TIMEOUT:
				//SYSTEMTIME time;
				//GetSystemTime(&time);
				//WORD milis = time.wSecond;
				//myFile << "a " << milis << "\n";
				dlg.DoTimerTask(timeoutCount);
				break;
		}
	}

	//myFile.close();


	
	/*MSG msg;
	UINT_PTR timerId = SetTimer(NULL, 0, timer * 1000, (TIMERPROC) NULL);
	int counter = 0;
	while(GetMessage(&msg, NULL, 0, 0)){
		//dlg.DoTimerTask(timeoutCount);
		SYSTEMTIME st;
		std::cout << "Mensaje [" << counter << "] a las " << ""  << std::endl;
		counter++;
		DispatchMessage(&msg);
	}
	
	KillTimer(NULL, timerId);*/
	//dlg.DoTimerTask();

	//if (nResponse == IDOK)
	//{
		
		// TODO: Place code here to handle when the dialog is
		//  dismissed with OK
		
	//}
	//else if (nResponse == IDCANCEL)
	//{
		// TODO: Place code here to handle when the dialog is
		//  dismissed with Cancel
		
	//}

    


	// Since the dialog has been closed, return FALSE so that we exit the
	//  application, rather than start the application's message pump.
	return FALSE;
}




int CWatchDogApp::ExitInstance()
{
	return CWinApp::ExitInstance();

}