package com.mcnc.bizmob.util.contact;

import com.mcnc.bizmob.core.util.log.Logger;

/**
 * 01.클래스 설명 : 연락처 정보를 담을 DTO class. <br>
 * 02.제품구분 : bizMob 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 연락처 정보를 담는 DTO <br>
 * 04.관련 API/화면/서비스 : AddressBookListItem, SearchHelperForDeviceContact, ContactPlugin <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-26                                    박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class AddressBookListItem {

	/**
	 * total Count.
	 */
	private int totalCount = 0;
	/**
	 * box path
	 */
	private String boxPath = "";
	/**
	 * 고유 Id
	 */
	private String UID = "";
	/**
	 * 이름
	 */
	private String displayName = "";
	/**
	 * 휴대폰 번호
	 */
	private String mobilePhone = "";
	/**
	 * 이메일 주소
	 */
	private String emailAddress = "";
	/**
	 * 직급
	 */
	private String grade = "";
	/**
	 * 부서
	 */
	private String depart = "";
	/**
	 * 회사명
	 */
	private String company = "";
	/**
	 * 팩스 번호
	 */
	private String faxNumber = "";
	/**
	 * 회사 전화번호
	 */
	private String companyPhone = "";

	public String getCompanyPhone() {
		return companyPhone;
	}

	public void setCompanyPhone(String companyPhone) {
		this.companyPhone = companyPhone;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getDepart() {
		return depart;
	}

	public void setDepart(String depart) {
		this.depart = depart;
	}

	public int getTotalCount() {
		Logger.d("test", "AddresBook totalCount" + totalCount);
		return totalCount;
	}

	public String getBoxPath() {
		return boxPath;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public void setBoxPath(String boxPath) {
		this.boxPath = boxPath;
	}

	public String getUID() {
		return UID;
	}

	public void setUID(String uID) {
		UID = uID;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getFaxNumber() {
		return faxNumber;
	}

	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}


}
