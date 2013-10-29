/*
 * The MIT License
 *
 * Copyright (c) 2011, Nigel Magnay / NiRiMa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.nirima.jenkins.repo.build;

import hudson.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.nirima.jenkins.repo.RepositoryDirectory;
import com.nirima.jenkins.repo.fs.VirtualRepositoryItem;
/**
 * Represent a maven repository item.
 */
public class MetadataMD5RepositoryItem extends VirtualRepositoryItem {
    
    private static final String MD_FILE_NAME_MD5 = "maven-metadata.xml.md5";
    
    private String content = null;
    
    private MetadataRepositoryItem item = null;
    
    public MetadataMD5RepositoryItem(MetadataRepositoryItem item, RepositoryDirectory parent) {
        super(parent, MD_FILE_NAME_MD5);
        this.item = item;
    }

    public String getName() {
        return MD_FILE_NAME_MD5;
    }

    public InputStream getContent() throws Exception {
        createContent();
        return new ByteArrayInputStream(content.getBytes("UTF-8"));
    }

    private String createContent() throws Exception, IOException {
        InputStream stream = item.getContent();
        content = Util.getDigestOf(stream);
        return content;
    }

    protected long getLastModTimeStamp() {
        return item.getLastModTimeStamp();
    }

    public Long getSize() {
        return 32L;
    }
}
