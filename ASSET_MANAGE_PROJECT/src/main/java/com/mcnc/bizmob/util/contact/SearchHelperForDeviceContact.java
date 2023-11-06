package com.mcnc.bizmob.util.contact;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.RawContacts;

import com.mcnc.bizmob.core.util.contact.AddressBookListItem;
import com.mcnc.bizmob.core.util.log.Logger;

import java.util.ArrayList;

/**
 * 01.클래스 설명 : 디바이스 주소록 검색을 위한 Helper Class <br>
 * 02.제품구분 : bizMob 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : 디바이스 주소록 검색 <br>
 * 04.관련 API/화면/서비스 : AddressBookListItem, ContactPlugin <br>
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
public class SearchHelperForDeviceContact {
	/**
	 * 개별 연락처 조회 사용할 쿼리의 컬럼 조건.
	 */
	private static String[] projection = new String[] {
			ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};

	/**
	 * 검색 조건 을 입력 받아, 연락처 검색하는 메소드.<br>
	 * <p>
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-26                           박재민                             최초 작성<br>
	 *
	 * </pre>
	 * @param mActivity Activity 객체.
	 * @param searchType 검색 종류, (ex. "phone", "name" 등)
	 * @param searchText 검색 keyword.
	 * @return 검색된 연락처 목록.
	 */
	public static ArrayList<AddressBookListItem> getSearchListResult(
			Activity mActivity, String searchType, String searchText) {

		ArrayList<AddressBookListItem> addressBookItemList = new ArrayList<AddressBookListItem>();

		Uri uri = null;
		String[] projection = null;
		String[] selectionArgs = null;
		StringBuilder selection = new StringBuilder();
		StringBuilder sortOrder = new StringBuilder();
		Cursor basicCursor = null;
		Cursor cursor = null;

		try {
			/** URI : 주소록 상세 데이터 */
			uri = ContactsContract.Data.CONTENT_URI;

			/** 가져올 컬럼 목록 생성 */
			projection = new String[] { ContactsContract.Data.CONTACT_ID,
					ContactsContract.Contacts.DISPLAY_NAME };

			/** 정렬 구문 생성 */
			sortOrder.append(ContactsContract.Contacts.DISPLAY_NAME);
			sortOrder.append(" COLLATE LOCALIZED ASC");

			/** Where 조건 구문 생성 */
			selection.append(ContactsContract.Data.MIMETYPE);
			selection.append(" = ?");
			selection.append(" AND ");
			selection.append(ContactsContract.Contacts.Data.DATA2);
			selection.append(" = ?");
			selection.append(" AND ");
			selection.append(ContactsContract.Contacts.Data.DATA1);
			selection.append(" LIKE '%'||?||'%'");

			if (searchType.equals("none")) {
				// 핸드폰 검색
				selectionArgs = new String[] {
						Phone.CONTENT_ITEM_TYPE,
						String
								.valueOf(Phone.TYPE_MOBILE),
						"" };

			} else if (searchType.equals("name")) {
				// 이름 검색
				selection = new StringBuilder();
				selection.append(ContactsContract.Contacts.DISPLAY_NAME);
				selection.append(" LIKE '%'||?||'%'");
				selectionArgs = new String[] { searchText };
			} else if (searchType.equals("phone")) {
				// 핸드폰 검색
				selectionArgs = new String[] {
						Phone.CONTENT_ITEM_TYPE,
						String
								.valueOf(Phone.TYPE_MOBILE),
						searchText };
			} else if (searchType.equals("email")) {
				// 이메일 검색
				selectionArgs = new String[] {
						ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
						String
								.valueOf(ContactsContract.CommonDataKinds.Email.TYPE_WORK),
						searchText };
			} else if (searchType.equals("dept")) {
				// 부서 검색
				selection = new StringBuilder();
				selection.append(ContactsContract.Data.MIMETYPE);
				selection.append(" = ?");
				selection.append(" AND ");
				selection.append(ContactsContract.Contacts.Data.DATA2);
				selection.append(" = ?");
				selection.append(" AND ");
				selection.append(ContactsContract.Contacts.Data.DATA5);
				selection.append(" LIKE '%'||?||'%'");

				selectionArgs = new String[] {
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
						String
								.valueOf(ContactsContract.CommonDataKinds.Organization.TYPE_WORK),
						searchText };
			} else if (searchType.equals("office_number")) {
				// 사무실 전화번호
				selectionArgs = new String[] {
						Phone.CONTENT_ITEM_TYPE,
						String
								.valueOf(Phone.TYPE_WORK),
						searchText };
			} else if (searchType.equals("company_name")) {
				// 회사이름
				selectionArgs = new String[] {
						ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
						String
								.valueOf(ContactsContract.CommonDataKinds.Organization.TYPE_WORK),
						searchText };
			} else if (searchType.equals("home_number")) {
				selectionArgs = new String[] {
						Phone.CONTENT_ITEM_TYPE,
						String
								.valueOf(Phone.TYPE_HOME),
						searchText };
			} else if (searchType.equals("group")) {
				// 그룹 검색
				selection.delete(0, selection.length());
				selection.append(GroupMembership.GROUP_ROW_ID);
				selection.append(" = ?");

				selectionArgs = new String[] { searchText };
			}

			/** Sqlite 실행 */
			//basicCursor = mActivity.managedQuery(uri, projection, selection.toString(), selectionArgs, sortOrder.toString());
			basicCursor = mActivity.getContentResolver().query(uri, projection, selection.toString(), selectionArgs, sortOrder.toString());

			/** Duplication Check */
			String duplicationCheckS = "";
			String duplicationCheckE = "";

			/** 가져올 컬럼 목록 생성 */
			projection = new String[] {

			ContactsContract.Data.CONTACT_ID,
					ContactsContract.Contacts.DISPLAY_NAME,
					ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1,
					ContactsContract.Data.DATA2

			};

			/** Where 조건 구문 생성 */
			selection = new StringBuilder();
			selection.append(ContactsContract.Data.CONTACT_ID);
			selection.append(" = ? AND ");
			selection.append(ContactsContract.Data.MIMETYPE);
			selection.append(" IN ( ?, ?, ? )");

			// basicCursor.
			while (basicCursor.moveToNext() && basicCursor.getPosition() < 3000) {
				/** 주소록 아이디 */
				Logger.d("address", "getPosition:" + basicCursor.getPosition());
				String contactId = basicCursor.getString(basicCursor
						.getColumnIndex(ContactsContract.Data.CONTACT_ID));
				/** 주소록 이름 */
				String contactName = basicCursor.getString(basicCursor
						.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				/** 중복 체크를 위한 아이디 셋팅 */
				duplicationCheckS = contactId;

				/** 중복여부 체크 */
				if (!duplicationCheckS.equals(duplicationCheckE)) {

					/** Where 조건 구문의 Value 셋팅 */
					selectionArgs = new String[] {
							contactId,
							Phone.CONTENT_ITEM_TYPE,
							ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
							ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
					/** Sqlite 실행 */
					//Cursor cursor = mActivity.managedQuery(uri, projection,selection.toString(), selectionArgs, null);
					cursor = mActivity.getContentResolver().query(uri, projection,selection.toString(), selectionArgs, null);
					if (cursor != null) {
						/** ContactItem 가져오기 */
						com.mcnc.bizmob.core.util.contact.AddressBookListItem addressBookItem = new com.mcnc.bizmob.core.util.contact.AddressBookListItem();
						/** 주소록 아이디 셋팅 */
						addressBookItem.setUID(contactId);
						/** 주소록 이름 셋팅 */
						addressBookItem.setDisplayName(contactName);
						while (cursor.moveToNext()) {
							/** Contents mimeType 셋팅 */
							String mimeType = cursor
									.getString(cursor
											.getColumnIndex(ContactsContract.Data.MIMETYPE));
							/** Contents type 셋팅 */
							int type = cursor
									.getInt(cursor
											.getColumnIndex(ContactsContract.Contacts.Data.DATA2));
							/** Contents data 셋팅 */
							String data = cursor
									.getString(cursor
											.getColumnIndex(ContactsContract.Contacts.Data.DATA1));

							if (mimeType
									.equals(Phone.CONTENT_ITEM_TYPE)
									&& type == Phone.TYPE_MOBILE) {
								/** 핸드폰 번호 셋팅 */
								addressBookItem.setMobilePhone(data);
								Logger.i("CONTACT GET_LIST DATA 핸드폰: ", data);
							} else if (mimeType
									.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
									&& type == ContactsContract.CommonDataKinds.Email.TYPE_WORK) {
								/** 이메일 주소 셋팅 */
								addressBookItem.setEmailAddress(data);
								Logger.i("CONTACT GET_LIST DATA 이메일: ", data);
							} else if (mimeType
									.equals(Phone.CONTENT_ITEM_TYPE)
									&& type == Phone.TYPE_FAX_WORK) {
								/** 팩스 번호 셋팅 */
								addressBookItem.setFaxNumber(data);
								Logger.i("CONTACT GET_LIST DATA 팩스: ", data);
							} else if (mimeType
									.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
									&& type == ContactsContract.CommonDataKinds.Organization.TYPE_WORK) {
								/** 회사 이름 셋팅 */
								addressBookItem.setCompany(data);
								Logger.i("CONTACT GET_LIST DATA 회사 이름: ", data);
							} else if (mimeType
									.equals(Phone.CONTENT_ITEM_TYPE)
									&& type == Phone.TYPE_COMPANY_MAIN) {
								/** 회사 이름 셋팅 */
								Logger.i("CONTACT GET_LIST DATA 회사 번호: ", data);
								
								data = cursor
										.getString(cursor
												.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
								
								addressBookItem.setCompanyPhone(data);
							}
						}
						if (!addressBookItem.getUID().equals(null)) {
							/** list에 contactItem 담기 */
							addressBookItemList.add(addressBookItem);
						}
						cursor.close();
						cursor = null;
					} else {
						System.out.println("내 폰연락처 보기 Cusor is null !!! "
								+ contactId);
						basicCursor.close();
						basicCursor = null;
						return addressBookItemList;
					}

				}
				duplicationCheckE = duplicationCheckS;
			}
			basicCursor.close();
			basicCursor = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if ( basicCursor != null ) {
				basicCursor.close();
			}
			if ( cursor != null ) {
				cursor.close();
			}
		}

		return addressBookItemList;
	}

	/**
	 * 연락처 고유 정보를 입력 받아 연락처를 삭제하는 메소드.<br>
	 * <p>
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-26                           박재민                             최초 작성<br>
	 *
	 * </pre>
	 * @param activity Activity 객체.
	 * @param contactID 삭제할 연락처의 고유 Id 값.
	 * @param contactName 삭제할 연락처의 이름
	 * @return 삭제된 연락처의 개수.
	 */
	public static int deletePerson(Activity activity,
								   String contactID, String contactName) throws RemoteException,
			OperationApplicationException {
		Logger.d("SearchHelperForDeviceContact", "deletePerson: " + contactName);
		ContentProviderResult[] result = null;
		int resultInt = -1;
		Cursor contactCursor = null;
		try {
			if (contactID != null &&  !(contactID.equals(""))) {
				resultInt = activity.getContentResolver().delete(
						RawContacts.CONTENT_URI, RawContacts.CONTACT_ID + " = ?",
						new String[] { contactID });
				
				Logger.d("SearchHelperForDeviceContact==", "deletePerson: " + contactName + " " + contactID);
				return resultInt;
			}
			if (contactName != null &&  !(contactName.equals(""))) {
				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
				Uri uri = null;
				
				String[] selectionArgs = null;
				StringBuilder selection = new StringBuilder();
				StringBuilder sortOrder = new StringBuilder();
				
				//연락처 정보를 가져오는 content uri
				uri = ContactsContract.Contacts.CONTENT_URI;
				
				//조건 설정 where 절 만드는것과 같음
				selection.append(ContactsContract.Contacts.DISPLAY_NAME + " =? ");
				selectionArgs = new String[]{contactName};
				//정렬
				sortOrder.append(ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
				//조회해서 가져온다
				//Cursor contactCursor = activity.managedQuery(uri, projection, selection.toString(), selectionArgs, sortOrder.toString());
				contactCursor = activity.getContentResolver().query(uri, projection, selection.toString(), selectionArgs, sortOrder.toString());
				
				Logger.d("address", "getPosition:" + contactCursor.getCount() + " " + contactID);
				while (contactCursor.moveToNext()) {
					for (int i = 0; i < contactCursor.getColumnCount(); i++) {
						Logger.d("address", ":" + contactCursor.getColumnName(i) + " " + contactCursor.getString(i));
					}
					if(contactCursor.getString(1).equals(contactName)) {
						resultInt = activity.getContentResolver().delete(
								RawContacts.CONTENT_URI,
								RawContacts.CONTACT_ID + " = ?",
								new String[] { contactCursor.getString(0) });
						Logger.d("address", "getPosition:" + " -- " + contactCursor.getString(1));
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if ( contactCursor != null ) {
				contactCursor.close();
			}
		}
		
		return resultInt;
	}


	/**
	 * 연락처 고유 정보를 입력 받아 연락처를 등록하는 메소드.<br>
	 * <p>
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-26                           박재민                             최초 작성<br>
	 *
	 * </pre>
	 *
	 * 
	 * @param activity activity 객체
	 * @param accountType 등록할 계정 타입
	 * @param accountName 등록할 계정 이름
	 * @param name 등록 이름
	 * @param phoneNumber 등록 연락처
	 * @param groupID 등록 그룹 아이디
	 * @return 연락처 추가 결과 배열
	 */
	public static ContentProviderResult[] addPersonContact(Activity activity,
														   String accountType, String accountName, String name,
														   String phoneNumber, String homeNumber, String groupID)
			throws RemoteException, OperationApplicationException {
		Logger.i("SearchHelperForDeviceContact", "addPersonContact: " + name);
		ContentProviderResult[] result = null;

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation.newInsert(
				RawContacts.CONTENT_URI).withValue(
				RawContacts.ACCOUNT_TYPE, accountType)
				.withValue(RawContacts.ACCOUNT_NAME,
						accountName).build());
		ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
					.withValue(
							ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
							name).build());
		if(phoneNumber != null && !phoneNumber.trim().equals("")) {
			ops.add(ContentProviderOperation.newInsert(
					ContactsContract.Data.CONTENT_URI).withValueBackReference(
					ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(
					ContactsContract.Data.MIMETYPE,
					Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER,
							phoneNumber).withValue(
							Phone.TYPE,
							Phone.TYPE_MOBILE).build());
			Logger.i("SearchHelperForDeviceContact", "phoneNumber: " + phoneNumber);
		}
		
		if(homeNumber != null && !homeNumber.trim().equals("")) {
			ops.add(ContentProviderOperation.newInsert(
					ContactsContract.Data.CONTENT_URI).withValueBackReference(
					ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(
					ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
					.withValue(Phone.NUMBER, homeNumber).withValue(Phone.TYPE,
							Phone.TYPE_COMPANY_MAIN).build());
			Logger.i("SearchHelperForDeviceContact", "homeNumber: " + homeNumber);
		}

		if(groupID != null && !groupID.trim().equals("")) {
			ops.add(ContentProviderOperation.newInsert(
					ContactsContract.Data.CONTENT_URI).withValueBackReference(
					ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(
					GroupMembership.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE)
					.withValue(GroupMembership.GROUP_ROW_ID, groupID).build());
			Logger.i("SearchHelperForDeviceContact", "groupID: " + groupID);
		}

		result = activity.getContentResolver().applyBatch(
				ContactsContract.AUTHORITY, ops);

		Logger.d("SearchHelperForDeviceContact", "Creating contact: " +result+ " " + accountType +" "+ accountName
				+" "+name
				+" "+phoneNumber
				+" "+homeNumber
				+" "+groupID);

		return result;
	}
}
