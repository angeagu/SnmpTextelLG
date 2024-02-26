// WatchDogDlg.h : header file
//

#pragma once

#include "afxwin.h"


// CWatchDogDlg dialog
class CWatchDogDlg : public CDialog
{
// Construction
public:
	CWatchDogDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	enum { IDD = IDD_WATCHDOG_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support

    HBITMAP m_hBitmap;
// Implementation
protected:
	HICON m_hIcon;

    
	// Generated message map functions
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()
public:
	HANDLE	m_hWdt;
	
	BOOL	m_bWatchdogEnable;
	BOOL	m_bCountMinute;
	DWORD	m_dwTimeoutCount;
	BOOL	m_bMouseReset;
	BOOL	m_bKeyboardReset;

	afx_msg void OnDestroy();
	afx_msg void OnClose();
	afx_msg void OnBnClickedRadioSecond();
	afx_msg void OnBnClickedRadioMinute();
	afx_msg void OnCbnSelchangeComboTimeoutcount();
	afx_msg void OnBnClickedBtnSet();
	afx_msg void OnBnClickedBtnClear();	

	void InitialOpenWDT();
	void LoadWdtState();
	void StoreWdtState();
	void DoTimerTask(int timer);
};
