//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'objectrelations' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package org.obrel.space;

import de.esoco.lib.expression.Function;

import java.io.File;


/********************************************************************
 * An object space implementation that maps URLs to the local file system.
 *
 * @author eso
 */
public class FileSystemSpace<T> extends RelationSpace<T>
{
	//~ Instance fields --------------------------------------------------------

	private final String	  sRootPath;
	private String			  sDefaultFile;
	private Function<File, T> fReadFile;

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance.
	 *
	 * @param sRootPath    The root path to which URLs are relative
	 * @param sDefaultFile The default file to look return on empty URLs (empty
	 *                     string for none)
	 * @param fReadFile    A function that reads a file and returns it's content
	 *                     with the datatype of this space
	 */
	public FileSystemSpace(String			 sRootPath,
						   String			 sDefaultFile,
						   Function<File, T> fReadFile)
	{
		if (!sRootPath.endsWith("/"))
		{
			sRootPath += "/";
		}

		this.sRootPath    = sRootPath;
		this.sDefaultFile = sDefaultFile;
		this.fReadFile    = fReadFile;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	public T get(String sUrl)
	{
		if (sUrl.startsWith("/"))
		{
			sUrl = sUrl.substring(1);
		}

		if (sUrl.isEmpty())
		{
			sUrl = sDefaultFile;
		}

		File aFile = new File(sRootPath + sUrl);

		if (!aFile.exists() || !aFile.isFile() || aFile.isHidden())
		{
			throw new IllegalArgumentException("Invalid URL: " + sUrl);
		}

		return fReadFile.evaluate(aFile);
	}
}
