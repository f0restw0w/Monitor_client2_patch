package com.m1.android.data.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import com.m1.android.data.entity.Contact;
import com.m1.android.data.util.LocalManager;
import com.m1.android.data.util.PhoneUtil;

public class ContactDao {
	public static final String SIM_CONTENT_URI = "content://icc/adn";

	public static final List<Contact> readContact(Context context) {
		try {
			Map<String, Contact> contacts = new HashMap<String, Contact>();
			readPhoneContact(context, contacts);
			readSimContact(context, contacts);
			List<Contact> uploadContacts = LocalManager.getContactList(context);
			// 过滤已上传的
			if (uploadContacts != null && !uploadContacts.isEmpty()) {
				for (int i = 0; i < uploadContacts.size(); i++) {
					Contact contact = uploadContacts.get(i);
					if (contact != null) {
						String number = contact.getPhone();
						if (contacts.containsKey(number)) {
							contacts.remove(number);
						}
					}
				}
			}
			if (!contacts.isEmpty()) {
				Collection<Contact> mContact = contacts.values();
				return new ArrayList<Contact>(mContact);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static final void readPhoneContact(Context context, Map<String, Contact> outContact) {
		try {
			ContentResolver resolver = context.getContentResolver();
			// 获取手机联系人
			Cursor cursor = resolver.query(Phone.CONTENT_URI, new String[] { Phone.DISPLAY_NAME, Phone.NUMBER }, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					// 得到手机号码
					String phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
					// 当手机号码为空的或者为空字段 跳过当前循环
					if (TextUtils.isEmpty(phoneNumber))
						continue;
					phoneNumber = PhoneUtil.formatPhone(phoneNumber);
					if (!outContact.containsKey(phoneNumber)) {
						// 得到联系人名称
						String contactName = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
						Contact contact = new Contact();
						contact.setName(contactName);
						contact.setPhone(phoneNumber);
						outContact.put(phoneNumber, contact);
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final void readSimContact(Context context, Map<String, Contact> outContact) {
		try {
			ContentResolver resolver = context.getContentResolver();
			Uri simUri = Uri.parse(SIM_CONTENT_URI);
			Cursor cursor = resolver.query(simUri, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					// 得到手机号码
					String phoneNumber = cursor.getString(cursor.getColumnIndex("number"));
					// 当手机号码为空的或者为空字段 跳过当前循环
					if (TextUtils.isEmpty(phoneNumber))
						continue;
					phoneNumber = PhoneUtil.formatPhone(phoneNumber);
					if (!outContact.containsKey(phoneNumber)) {
						// 得到联系人名称
						String contactName = cursor.getString(cursor.getColumnIndex("name"));
						Contact contact = new Contact();
						contact.setName(contactName);
						contact.setPhone(phoneNumber);
						outContact.put(phoneNumber, contact);
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
