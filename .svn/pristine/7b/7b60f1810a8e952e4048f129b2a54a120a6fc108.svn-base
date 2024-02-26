// WatchDogDlg.cpp : implementation file
//

#include "stdafx.h"
#include "WatchDog.h"
#include "WatchDogDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

// Implementation
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
END_MESSAGE_MAP()


// CWatchDogDlg dialog




CWatchDogDlg::CWatchDogDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CWatchDogDlg::IDD, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
   
	m_hWdt = NULL;
	m_bWatchdogEnable	= FALSE;
	m_bMouseReset		= FALSE;
	m_bKeyboardReset	= FALSE;
	m_dwTimeoutCount    = 0;


    //Step1 Open the watchdog function from aaeonWdtOpen()
	InitialOpenWDT();
}

void CWatchDogDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	
    if( !pDX->m_bSaveAndValidate )
	{
		if( m_hWdt )
		{
			LoadWdtState();

			CComboBox* pcbTimeout = (CComboBox*)GetDlgItem( IDC_CMB_TIMEOUTCOUNT );
			if( pcbTimeout )
				for( int i = 0 ; i < pcbTimeout->GetCount() ; i++ )
				{
					if( pcbTimeout->GetItemData(i) >= m_dwTimeoutCount )
					{
						if( pcbTimeout->GetCurSel() != i )
							pcbTimeout->SetCurSel( i );
						break;
					}
				}
		}
	}
	
	if( pDX->m_bSaveAndValidate )
	{
		if( m_hWdt )
		{	
			CComboBox* pcbTimeout = (CComboBox*)GetDlgItem( IDC_CMB_TIMEOUTCOUNT );
			int index = pcbTimeout->GetCurSel();
			m_dwTimeoutCount = (DWORD)pcbTimeout->GetItemData(index);
		}
	}
}

BEGIN_MESSAGE_MAP(CWatchDogDlg, CDialog)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP
	ON_WM_DESTROY()
	ON_WM_CLOSE()
	ON_BN_CLICKED(IDC_RADIO_SECOND, &CWatchDogDlg::OnBnClickedRadioSecond)
	ON_BN_CLICKED(IDC_RADIO_MINUTE, &CWatchDogDlg::OnBnClickedRadioMinute)
	ON_BN_CLICKED(IDC_BTN_SET, &CWatchDogDlg::OnBnClickedBtnSet)
	ON_BN_CLICKED(IDC_BTN_CLEAR, &CWatchDogDlg::OnBnClickedBtnClear)
	ON_CBN_SELCHANGE(IDC_CMB_TIMEOUTCOUNT, &CWatchDogDlg::OnCbnSelchangeComboTimeoutcount)

END_MESSAGE_MAP()


// CWatchDogDlg message handlers

BOOL CWatchDogDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon

	// TODO: Add extra initialization here
    CString s;

	CComboBox* pcbTimeout = (CComboBox*)GetDlgItem( IDC_CMB_TIMEOUTCOUNT );
	if( pcbTimeout )
	{
		for( int i = 0 ; i < 256 ; i++ )
		{
			s.Format( _T("%03d"), i );
			int index = pcbTimeout->AddString( s );
			pcbTimeout->SetItemData( index, i );
		}
	}
	
	if(!m_bCountMinute)
	{
		CButton* RadFirst = (CButton*)GetDlgItem(IDC_RADIO_SECOND);
		RadFirst->SetCheck(BST_CHECKED);
		SetDlgItemTextW(IDC_STATIC_WDT_TIMECOUNT_UNIT,_T("Sec."));
	}
	else
	{
		CButton* RadSec = (CButton*)GetDlgItem(IDC_RADIO_MINUTE);
		RadSec->SetCheck(BST_CHECKED);
		SetDlgItemTextW(IDC_STATIC_WDT_TIMECOUNT_UNIT,_T("Min."));
	}

	if( pcbTimeout )
	{
		for( int i = 0 ; i < pcbTimeout->GetCount() ; i++ )
		{
			if( pcbTimeout->GetItemData(i) >= m_dwTimeoutCount )
			{
				if( pcbTimeout->GetCurSel() != i )
					pcbTimeout->SetCurSel( i );
				break;
			}
		}
	}

	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CWatchDogDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CWatchDogDlg::OnPaint()
{
    CPaintDC dc(this); // device context for painting
	
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this function to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CWatchDogDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void CWatchDogDlg::OnDestroy()
{
	CDialog::OnDestroy();
	// TODO: Add your message handler code here
}

void CWatchDogDlg::OnClose()
{
	// TODO: Add your message handler code here and/or call default

	if( m_hWdt )
	{
		m_bWatchdogEnable = FALSE;
		StoreWdtState();
		aaeonWdtClose(m_hWdt);
		m_hWdt = NULL;
	}

	CDialog::OnClose();
}

void CWatchDogDlg::InitialOpenWDT()
{
	m_hWdt = aaeonWdtOpen( NULL );

	if( m_hWdt == NULL )
	{
		DWORD dwErr = GetLastError();
		if( dwErr == ERROR_SUCCESS )
			AfxMessageBox( _T("No WatchDog device is Found!\n\nSystem Exit!"), MB_ICONSTOP );
		else
		{
			if( dwErr == ERROR_LOGON_FAILURE )
				AfxMessageBox( _T("The borad isn't Aaeon motherboard!\n\nSystem Exit!"), MB_ICONSTOP );
			else if( dwErr == ERROR_FILE_NOT_FOUND )
				AfxMessageBox( _T("Aaeon driver file (*.sys) isn't found!\n\nSystem Exit!"), MB_ICONSTOP );
			else
			{
				CString s; 
				s.Format( _T("Error found! code#%d\n\nSystem Exit!"), dwErr );
				AfxMessageBox( s, MB_ICONSTOP );
			}
		}
	}
}

void CWatchDogDlg::LoadWdtState()
{
	HRESULT hr;
    if(m_hWdt)
	{
		hr = aaeonWdtGetEnable( m_hWdt, &m_bWatchdogEnable );
		hr = aaeonWdtGetCountMode( m_hWdt, &m_bCountMinute);
		hr = aaeonWdtGetTimeoutCount( m_hWdt, &m_dwTimeoutCount );
		//hr = aaeonWdtGetMouseResetEnable( m_hWdt, &m_bMouseReset );
		//if( hr==(HRESULT)E_NOTIMPL )
		//	m_bMouseReset = -1;
		//hr = aaeonWdtGetKeyboardResetEnable( m_hWdt, &m_bKeyboardReset );
		//if( hr==(HRESULT)E_NOTIMPL )
		//	m_bKeyboardReset = -1;
	}
}

void CWatchDogDlg::StoreWdtState()
{
	HRESULT hr;

	if(m_hWdt)
	{
		hr = aaeonWdtSetEnable( m_hWdt, m_bWatchdogEnable );
		hr = aaeonWdtSetCountMode( m_hWdt, m_bCountMinute);
		hr = aaeonWdtSetTimeoutCount( m_hWdt, m_dwTimeoutCount );
		//hr = aaeonWdtSetMouseResetEnable( m_hWdt, m_bMouseReset );
		//hr = aaeonWdtSetKeyboardResetEnable( m_hWdt, m_bKeyboardReset );
	}
}

void CWatchDogDlg::OnBnClickedRadioSecond()
{
	// TODO: Add your control notification handler code here
	m_bCountMinute =FALSE;
    SetDlgItemTextW(IDC_STATIC_WDT_TIMECOUNT_UNIT,_T("Sec."));
}

void CWatchDogDlg::OnBnClickedRadioMinute()
{
	m_bCountMinute =TRUE;
	SetDlgItemTextW(IDC_STATIC_WDT_TIMECOUNT_UNIT,_T("Min."));	
}

void CWatchDogDlg::OnCbnSelchangeComboTimeoutcount()
{
	UpdateData( TRUE );
}


void CWatchDogDlg::DoTimerTask(int timer){

	

	//UpdateData(TRUE);

	m_bCountMinute = FALSE;
	m_bWatchdogEnable = TRUE;
	m_dwTimeoutCount = timer;

	//Step2: Set the timedownocunt to aaeonWdtSetTimeoutCount() 
	//       Set time mode (min or sec) to aaeonWdtSetCountMode()
	//       enable watchdog to reboot from aaeonWdtSetEnable()         
	StoreWdtState();
	LoadWdtState();
	
	//UpdateData( FALSE );

}

void CWatchDogDlg::OnBnClickedBtnSet()
{
	CButton* RadFirst = (CButton*)GetDlgItem(IDC_RADIO_SECOND);

	if(RadFirst->GetCheck() == BST_CHECKED)
		m_bCountMinute =FALSE;
	else
		m_bCountMinute =TRUE;


	UpdateData(TRUE);

	m_bWatchdogEnable = TRUE;

	//Step2: Set the timedownocunt to aaeonWdtSetTimeoutCount() 
	//       Set time mode (min or sec) to aaeonWdtSetCountMode()
	//       enable watchdog to reboot from aaeonWdtSetEnable()         
	StoreWdtState();
	LoadWdtState();
	
	UpdateData( FALSE );


	
	
}

void CWatchDogDlg::OnBnClickedBtnClear()
{
	m_dwTimeoutCount = 0;

	StoreWdtState();
	LoadWdtState();

	CButton* RadFirst = (CButton*)GetDlgItem(IDC_RADIO_SECOND);
	CButton* RadSec = (CButton*)GetDlgItem(IDC_RADIO_MINUTE);
	if(!m_bCountMinute)
	{
		RadFirst->SetCheck(BST_CHECKED);
		RadSec->SetCheck(BST_UNCHECKED);
		SetDlgItemTextW(IDC_STATIC_WDT_TIMECOUNT_UNIT,_T("Sec."));
	}
	else
	{
		RadSec->SetCheck(BST_CHECKED);
		RadFirst->SetCheck(BST_UNCHECKED);
		SetDlgItemTextW(IDC_STATIC_WDT_TIMECOUNT_UNIT,_T("Min."));
	}
	
	UpdateData( FALSE );
}

