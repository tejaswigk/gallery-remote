/*
 *  Gallery Remote - a File Upload Utility for Gallery
 *
 *  Gallery - a web based photo album viewer and editor
 *  Copyright (C) 2000-2001 Bharat Mediratta
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.gallery.GalleryRemote;

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import com.gallery.GalleryRemote.model.*;

/**
 *  Thumbnail cache loads and resizes images in the background for display in
 *  the list of Pictures
 *
 *@author     paour
 *@created    August 16, 2002
 */
public class ThumbnailCache implements Runnable
{
	public static final String MODULE = "ThumbCache";
	
	boolean stillRunning = false;
	Stack toLoad = new Stack();
	Hashtable thumbnails = new Hashtable();
	MainFrame mf;


	/**
	 *  Constructor for the ThumbnailCache object
	 *
	 *@param  mf  Description of Parameter
	 */
	public ThumbnailCache( MainFrame mf ) {
		this.mf = mf;
	}


	/**
	 *  Main processing method for the ThumbnailLoader object
	 */
	public void run() {
		//Log.log(Log.TRACE, MODULE, "Starting " + iFilename);
		while ( !toLoad.isEmpty() ) {
			String filename = (String) toLoad.pop();

			if ( thumbnails.get( filename ) == null ) {
				loadThumbnail( filename );

				mf.thumbnailLoadedNotify();
			}
		}
		stillRunning = false;

		//Log.log(Log.TRACE, MODULE, "Ending");
	}


	/**
	 *  Ask for the thumbnail to be loaded
	 *
	 *@param  filename  path to the file
	 */
	public void preloadThumbnail( String filename ) {
		Log.log(Log.TRACE, MODULE, "loadPreview " + filename);

		toLoad.add( 0, filename );

		rerun();
	}


	/**
	 *  Ask for the thumbnail to be loaded as soon as possible
	 *
	 *@param  filename  path to the file
	 */
	public void preloadThumbnailFirst( String filename ) {
		Log.log(Log.TRACE, MODULE, "loadPreview " + filename);

		toLoad.push( filename );

		rerun();
	}


	/**
	 *  Ask for several thumnails to be loaded
	 *
	 *@param  files  enumeration of File objects that should be loaded
	 */
	public void preloadThumbnailFiles( Enumeration files ) {
		Log.log(Log.TRACE, MODULE, "loadPreview " + files);

		while ( files.hasMoreElements() ) {
			toLoad.add( 0, ( (Picture) files.nextElement() ).getSource().getPath() );
		}

		rerun();
	}


	/**
	 *  Ask for several thumnails to be loaded
	 *
	 *@param  filenames  an array of File objects
	 */
	public void preloadThumbnails( File[] filenames ) {
		Log.log(Log.TRACE, MODULE, "loadPreview " + filenames);

		for ( int i = 0; i < filenames.length; i++ ) {
			toLoad.add( 0, ( (File) filenames[i] ).getPath() );
		}

		rerun();
	}


	void rerun() {
		if ( !stillRunning && GalleryRemote.getInstance().properties.getShowThumbnails() ) {
			stillRunning = true;
			Log.log(Log.TRACE, MODULE, "Calling Start");
			new Thread( this ).start();
		}
	}


	void cancelLoad() {
		toLoad.clear();
	}


	/**
	 *  Perform the actual icon loading
	 *
	 *@param  filename  path to the file
	 *@return           Resized icon
	 */
	public ImageIcon loadThumbnail( String filename ) {
		ImageIcon r = new ImageIcon( filename );
		Dimension d = PreviewFrame.getSizeKeepRatio( new Dimension( r.getIconWidth(), r.getIconHeight() ), GalleryRemote.getInstance().properties.getThumbnailSize() );

		long start = System.currentTimeMillis();
		Image scaled = r.getImage().getScaledInstance( d.width, d.height, mf.highQualityThumbnails ? Image.SCALE_SMOOTH : Image.SCALE_FAST );
		r.getImage().flush();
		r.setImage( scaled );

		thumbnails.put( filename, r );
		Log.log(Log.TRACE, MODULE, "Time: " + ( System.currentTimeMillis() - start ) );

		return r;
	}


	/**
	 *  Retrieves a thumbnail from the thumnail cache
	 *
	 *@param  filename  path to the file
	 *@return           The thumbnail object
	 */
	public ImageIcon getThumbnail( String filename ) {
		return (ImageIcon) thumbnails.get( filename );
	}
}
