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

/**
 * An object space implementation that maps URLs to the local file system.
 *
 * @author eso
 */
public class FileSystemSpace<T> extends RelationSpace<T> {

	private final String rootPath;

	private final String defaultFile;

	private final Function<File, T> readFile;

	/**
	 * Creates a new instance.
	 *
	 * @param rootPath    The root path to which URLs are relative
	 * @param defaultFile The default file to look return on empty URLs (empty
	 *                    string for none)
	 * @param readFile    A function that reads a file and returns it's content
	 *                    with the datatype of this space
	 */
	public FileSystemSpace(String rootPath, String defaultFile,
		Function<File, T> readFile) {
		if (!rootPath.endsWith("/")) {
			rootPath += "/";
		}

		this.rootPath = rootPath;
		this.defaultFile = defaultFile;
		this.readFile = readFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get(String url) {
		if (url.startsWith("/")) {
			url = url.substring(1);
		}

		if (url.isEmpty()) {
			url = defaultFile;
		}

		File file = new File(rootPath + url);

		if (!file.exists() || !file.isFile() || file.isHidden()) {
			throw new IllegalArgumentException("Invalid URL: " + url);
		}

		return readFile.evaluate(file);
	}
}
