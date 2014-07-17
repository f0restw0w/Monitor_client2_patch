package com.m1.android.data.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;

import com.m1.android.data.entity.Album;

public class AlbumUtil {
	public static final List<Album> getAlbums(Context context, long id) {
		Cursor cursor = null;
		if (id == 0) {
			cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, ImageColumns._ID + " desc limit 3");
		} else {
			cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, ImageColumns._ID + ">?",
					new String[] { String.valueOf(id) }, ImageColumns._ID + " desc ");
		}
		List<Album> albums = null;
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				albums = new ArrayList<Album>();
				while (cursor.moveToNext()) {
					Album album = readFromCursor(context, cursor);
					if (album != null) {
						albums.add(album);
					}
				}
			}
			cursor.close();
		}
		// 对相册数据重新排序
		if (albums != null && !albums.isEmpty()) {
			Collections.sort(albums, new AlbumComparator());
		}
		return albums;
	}

	private static final Album readFromCursor(Context context, Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(ImageColumns._ID));
		if (id <= 0)
			return null;
		String imagePath = cursor.getString(cursor.getColumnIndex(ImageColumns.DATA));
		long time = cursor.getLong(cursor.getColumnIndex(ImageColumns.DATE_ADDED));
		String thumbnailPath = null;
		Cursor c = Thumbnails.queryMiniThumbnail(context.getContentResolver(), id, Thumbnails.MINI_KIND, new String[] { Thumbnails.DATA });
		if (c != null) {
			if (c.getCount() > 0) {
				c.moveToFirst();
				thumbnailPath = cursor.getString(cursor.getColumnIndex(Thumbnails.DATA));
			}
			c.close();
		}
		String path = TextUtils.isEmpty(thumbnailPath) ? imagePath : thumbnailPath;
		Album album = new Album();
		album.setAddTime(time);
		album.setId(id);
		album.setPath(path);
		return album;
	}

}

class AlbumComparator implements Comparator<Album> {

	@Override
	public int compare(Album lhs, Album rhs) {
		if (lhs.getId() > rhs.getId()) {
			return 1;
		} else if (lhs.getId() == rhs.getId()) {
			return 0;
		} else {
			return -1;
		}
	}

}
