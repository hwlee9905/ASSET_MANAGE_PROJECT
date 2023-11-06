package com.mcnc.bizmob.fcm;

import android.content.Context;
import android.content.SharedPreferences;

import com.mcnc.bizmob.core.util.log.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 01.클래스 설명 : Push 메세지 이력을 관해주는 클래스. <br>
 * 02.제품구분 : bizMOB 3.0 Android Container <br>
 * 03.기능(콤퍼넌트) 명 : push message 이력 관리 <br>
 * 04.관련 API/화면/서비스 : SharedPreferences, Logger <br>
 * 05.수정이력  <br>
 * <pre>
 * **********************************************************************************************************************************<br>
 *  수정일                                          이름                          변경 내용<br>
 * **********************************************************************************************************************************<br>
 *  2016-09-22                                     박재민                         최초 작성<br>
 * **********************************************************************************************************************************<br>
 * </pre>
 *
 * @author 박재민
 * @version 1.0
 */
public class PushHistory {

	/**
	 * Push message의 고유 id list
	 */
	private ArrayList<String> messageList;

	/**
	 * push history 저장용 SharedPreferences 객체
	 */
	private SharedPreferences preferences;

	/**
	 * SharedPreferences 편집 작업용 Editor 객체
	 */
	private SharedPreferences.Editor editor;

	/**
	 * push history 를 저장할 최대 개수 값
	 */
	private int maxSize;

	/**
	 * Push history를 저장할 list의 최대 사이즈가 10으로 고정된 생성자.<br>
	 *
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-22                           박재민                             최초 작성<br>
	 *
	 *</pre>
	 *
	 * @param context context 객체 
	 */
	public PushHistory(Context context) {
		init(context, 10);
	}

	/**
	 * Push history를 저장할 list의 최대 사이즈를 받을 수 있는 생성자.<br>
	 *
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-22                           박재민                             최초 작성<br>
	 *
	 *</pre>
	 *
	 * @param context context 객체 
	 * @param maxSize 최대 push histroy 사이즈 
	 */
	public PushHistory(Context context, int maxSize) {
		init(context, maxSize);
	}

	/**
	 * 저장소(SharedPreferences를 생성하고, messagelist를 초기화 하는 메소드.<br>
	 *
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-22                           박재민                             최초 작성<br>
	 *
	 *</pre>
	 *
	 * @param context context 객체 
	 * @param maxSize 최대 push histroy 사이즈 
	 */
	public void init(Context context, int maxSize){
		
		this.maxSize = maxSize;
		preferences = context.getSharedPreferences("pushHistory", Context.MODE_PRIVATE);
		editor = preferences.edit();
		messageList = new ArrayList<String>();
		
		Map<String, ?> map = preferences.getAll();
		
		String[] temp = new String[map.size()];
		
		for(Entry<String, ?> entry : map.entrySet()){
			
			String key = entry.getKey();
			//base작업
			String value = "";
			if(entry.getValue() != null){
				value = entry.getValue().toString();
			}
			
			Logger.i("init", "key : " + key + " / value : " + value);
			
			temp[Integer.parseInt(key)] = value;
		}
		
		for(int i = 0; i < temp.length; i++){
			messageList.add(temp[i]);
		}
	}


	/**
	 * push message 고유 id를 list에 추가하는 메소드.<br>
	 *
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-22                           박재민                             최초 작성<br>
	 *
	 *</pre>
	 *
	 * @param messageID push message의 고유 id
	 */
	public void setMessageID(String messageID){
		
		if(messageList.size() < maxSize){
		
			messageList.add(messageID);
		} else {
			
			messageList.remove(0);
			messageList.add(messageID);
		}
		
		commitPref();
	}

	/**
	 * push message id가 message list에 존재하는지 확인 하는 메소드.<br>
	 *
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-22                           박재민                             최초 작성<br>
	 *
	 *</pre>
	 *
	 * @param messageID push message의 고유 id
	 * @return 존재 여부 (true, false)
	 */
	public boolean hasMessageID(String messageID){
		
		if(messageID != null){
			
			return messageList.contains(messageID);
		} else {
			return false;
		}
		
	}

	/**
	 * SharedPreferences에 message list 정보 하나씩 빼서 저장하는 메소드.<br>
	 *
	 * <pre>
	 * 수정이력 <br>
	 * **********************************************************************************************************************************<br>
	 *  수정일                               이름                               변경내용<br>
	 * **********************************************************************************************************************************<br>
	 *  2016-09-22                           박재민                             최초 작성<br>
	 *
	 *</pre>
	 *
	 * @return commit 성공 여부 (true, false)
	 */
	private boolean commitPref(){
		
		editor.clear();
		
		for(int i = 0; i < messageList.size(); i++){
			
			Logger.i("setMessageID", "" + messageList.get(i));
			
			editor.putString(String.valueOf(i), messageList.get(i));
			
		}
		
		return editor.commit();
	}
}
