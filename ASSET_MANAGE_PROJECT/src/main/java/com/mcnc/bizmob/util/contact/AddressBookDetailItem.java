package com.mcnc.bizmob.util.contact;

import android.graphics.Bitmap;

public class AddressBookDetailItem {
	private Bitmap profileImageBitmap;
	/** UID */
	private String UID = "";
	/** BoxId */
	private String boxId = "";
	/** 성 */
	private String FirstName = "";
	/** 이름 */
	private String Name = "";
	/** 애칭 */
	private String Nickname = "";
	/** 그림 */
	private String Image = "";
	/** 회사명 */
	private String Company_Name = "";
	/** 부서 */
	private String Company_Department = "";
	/** 직함 */
	private String Grade = "";
	/** 표시방법 */
	private String Display_Method = "";
	/** 회사전화1 */
	private String Company_Tel = "";
	/** 회사전화2 */
	private String Company_Tel2 = "";
	/** 회사팩스 */
	private String Company_Fax = "";
	/** 회사주소_우편번호 */
	private String Company_Post = "";
	/** 회사주소_시/도 */
	private String Company_CityState = "";
	/** 회사주소_구/군/시 */
	private String Company_GuGunCity = "";
	/** 회사주소_나머지주소 */
	private String Company_DetailAddress = "";
	/** 회사주소_국가 */
	private String Company_Country = "";
	/** 인스턴스 메신저1 */
	private String IM = "";
	/** 인스턴스 메신저2 */
	private String IM2 = "";
	/** 인스턴스 메신저3 */
	private String IM3 = "";
	/** 전자메일1 */
	private String Email = "";
	/** 전자메일2 */
	private String Email2 = "";
	/** 전자메일3 */
	private String Email3 = "";
	/** 휴대폰 */
	private String Mobile = "";
	/** 웹페이지 */
	private String WebPage = "";
	/** 사무실이름 */
	private String Company_OfficeName = "";
	/** 집전화1 */
	private String Home_Tel = "";
	/** 집전화2 */
	private String Home_Tel2 = "";
	/** 집주소_우편번호 */
	private String Home_Post = "";
	/** 집주소_시/도 */
	private String Home_CityState = "";
	/** 집주소_구/군/시 */
	private String Home_GuGunCity = "";
	/** 집주소_나머지주소 */
	private String Home_DetailAddress = "";
	/** 집주소_국가 */
	private String Home_Country = "";
	/** 기타주소_우편번호 */
	private String Other_Post = "";
	/** 기타주소_시/도 */
	private String Other_CityState = "";
	/** 기타주소_구/군/시 */
	private String Other_GuGunCity = "";
	/** 기타주소_나머지주소 */
	private String Other_DetailAddress = "";
	/** 기타주소_국가 */
	private String Other_Country = "";
	/** 비서 */
	private String Secretary_Name = "";
	/** 비서전화 */
	private String Secretary_Tel = "";
	/** 생일 */
	private String Birthday = "";
	/** 결혼기념일 */
	private String Anniversary = "";
	/** 메모 */
	private String Memo = "";
	/** 관리자 */
	private String Administrator = "";
	/** 대표번호 타이틀 */
	private String mainNumberTitle = "";
	/** 대표번호 */
	private String mainNumber = "";
	/** 사진 url */
	private String photoId = "";
	
	public String getUID() {
		return UID;
	}
	public void setUID(String uID) {
		UID = uID;
	}
	public String getBoxId() {
		return boxId;
	}
	public void setBoxId(String boxPath) {
		this.boxId = boxPath;
	}
	
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
	public String getMainNumberTitle() {
		return mainNumberTitle;
	}
	public void setMainNumberTitle(String mainNumberTitle) {
		this.mainNumberTitle = mainNumberTitle;
	}
	public String getMainNumber() {
		return mainNumber;
	}
	public void setMainNumber(String mainNumber) {
		this.mainNumber = mainNumber;
	}
	public Bitmap getProfileImageBitmap() {
		return profileImageBitmap;
	}
	public void setProfileImageBitmap(Bitmap profileImageBitmap) {
		this.profileImageBitmap = profileImageBitmap;
	}
	public String getFirstName() {
		return FirstName;
	}
	public String getName() {
		return Name;
	}
	
	public String getFullName(){
		return getFirstName() + getName();
	}
	
	public String getNickname() {
		return Nickname;
	}
	public String getImage() {
		return Image;
	}
	public String getCompany_Name() {
		return Company_Name;
	}
	public String getCompany_Department() {
		return Company_Department;
	}
	public String getGrade() {
		return Grade;
	}
	public String getDisplay_Method() {
		return Display_Method;
	}
	public String getCompany_Tel() {
		return Company_Tel;
	}
	public String getCompany_Tel2() {
		return Company_Tel2;
	}
	public String getCompany_Fax() {
		return Company_Fax;
	}
	public String getCompany_Post() {
		return Company_Post;
	}
	public String getCompany_CityState() {
		return Company_CityState;
	}
	public String getCompany_GuGunCity() {
		return Company_GuGunCity;
	}
	public String getCompany_DetailAddress() {
		return Company_DetailAddress;
	}
	public String getCompany_Country() {
		return Company_Country;
	}
	
	public String getCompanyAddress(){
		
		return getCompany_Country()+getCompany_CityState()+getCompany_GuGunCity()+getCompany_DetailAddress()+getCompany_Post();
	}

	public String getIM() {
		return IM;
	}
	public String getIM2() {
		return IM2;
	}
	public String getIM3() {
		return IM3;
	}
	public String getEmail() {
		return Email;
	}
	public String getEmail2() {
		return Email2;
	}
	public String getEmail3() {
		return Email3;
	}
	public String getMobile() {
		return Mobile;
	}
	public String getWebPage() {
		return WebPage;
	}
	public String getCompany_OfficeName() {
		return Company_OfficeName;
	}
	public String getHome_Tel() {
		return Home_Tel;
	}
	public String getHome_Tel2() {
		return Home_Tel2;
	}
	public String getHome_Post() {
		return Home_Post;
	}
	public String getHome_CityState() {
		return Home_CityState;
	}
	public String getHome_GuGunCity() {
		return Home_GuGunCity;
	}
	public String getHome_DetailAddress() {
		return Home_DetailAddress;
	}
	public String getHome_Country() {
		return Home_Country;
	}
	public String getHomeAddress(){
		return getHome_Country()+getHome_CityState()+getHome_GuGunCity()+getHome_DetailAddress()+getHome_Post();
	}
	
	public String getOther_Post() {
		return Other_Post;
	}
	public String getOther_CityState() {
		return Other_CityState;
	}
	public String getOther_GuGunCity() {
		return Other_GuGunCity;
	}
	public String getOther_DetailAddress() {
		return Other_DetailAddress;
	}
	public String getOther_Country() {
		return Other_Country;
	}
	
	public String getOtherAddress(){
		return getOther_Country()+getOther_CityState()+getOther_GuGunCity()+getOther_DetailAddress()+getOther_Post();
	}
	
	public String getSecretary_Name() {
		return Secretary_Name;
	}
	public String getSecretary_Tel() {
		return Secretary_Tel;
	}
	public String getBirthday() {
		return Birthday;
	}
	public String getAnniversary() {
		return Anniversary;
	}
	public String getMemo() {
		return Memo;
	}
	public String getAdministrator() {
		return Administrator;
	}
	public void setFirstName(String firstName) {
		FirstName = firstName;
	}
	public void setName(String name) {
		Name = name;
	}
	public void setNickname(String nickname) {
		Nickname = nickname;
	}
	public void setImage(String image) {
		Image = image;
	}
	public void setCompany_Name(String companyName) {
		Company_Name = companyName;
	}
	public void setCompany_Department(String companyDepartment) {
		Company_Department = companyDepartment;
	}
	public void setGrade(String grade) {
		Grade = grade;
	}
	public void setDisplay_Method(String displayMethod) {
		Display_Method = displayMethod;
	}
	public void setCompany_Tel(String companyTel) {
		Company_Tel = companyTel;
	}
	public void setCompany_Tel2(String companyTel2) {
		Company_Tel2 = companyTel2;
	}
	public void setCompany_Fax(String companyFax) {
		Company_Fax = companyFax;
	}
	public void setCompany_Post(String companyPost) {
		Company_Post = companyPost;
	}
	public void setCompany_CityState(String companyCityState) {
		Company_CityState = companyCityState;
	}
	public void setCompany_GuGunCity(String companyGuGunCity) {
		Company_GuGunCity = companyGuGunCity;
	}
	public void setCompany_DetailAddress(String companyDetailAddress) {
		Company_DetailAddress = companyDetailAddress;
	}
	public void setCompany_Country(String companyCountry) {
		Company_Country = companyCountry;
	}
	public void setIM(String iM) {
		IM = iM;
	}
	public void setIM2(String iM2) {
		IM2 = iM2;
	}
	public void setIM3(String iM3) {
		IM3 = iM3;
	}
	public void setEmail(String email) {
		Email = email;
	}
	public void setEmail2(String email2) {
		Email2 = email2;
	}
	public void setEmail3(String email3) {
		Email3 = email3;
	}
	public void setMobile(String mobile) {
		Mobile = mobile;
	}
	public void setWebPage(String webPage) {
		WebPage = webPage;
	}
	public void setCompany_OfficeName(String companyOfficeName) {
		Company_OfficeName = companyOfficeName;
	}
	public void setHome_Tel(String homeTel) {
		Home_Tel = homeTel;
	}
	public void setHome_Tel2(String homeTel2) {
		Home_Tel2 = homeTel2;
	}
	public void setHome_Post(String homePost) {
		Home_Post = homePost;
	}
	public void setHome_CityState(String homeCityState) {
		Home_CityState = homeCityState;
	}
	public void setHome_GuGunCity(String homeGuGunCity) {
		Home_GuGunCity = homeGuGunCity;
	}
	public void setHome_DetailAddress(String homeDetailAddress) {
		Home_DetailAddress = homeDetailAddress;
	}
	public void setHome_Country(String homeCountry) {
		Home_Country = homeCountry;
	}
	public void setOther_Post(String otherPost) {
		Other_Post = otherPost;
	}
	public void setOther_CityState(String otherCityState) {
		Other_CityState = otherCityState;
	}
	public void setOther_GuGunCity(String otherGuGunCity) {
		Other_GuGunCity = otherGuGunCity;
	}
	public void setOther_DetailAddress(String otherDetailAddress) {
		Other_DetailAddress = otherDetailAddress;
	}
	public void setOther_Country(String otherCountry) {
		Other_Country = otherCountry;
	}
	public void setSecretary_Name(String secretaryName) {
		Secretary_Name = secretaryName;
	}
	public void setSecretary_Tel(String secretaryTel) {
		Secretary_Tel = secretaryTel;
	}
	public void setBirthday(String birthday) {
		Birthday = birthday;
	}
	public void setAnniversary(String anniversary) {
		Anniversary = anniversary;
	}
	public void setMemo(String memo) {
		Memo = memo;
	}
	public void setAdministrator(String administrator) {
		Administrator = administrator;
	}
}
