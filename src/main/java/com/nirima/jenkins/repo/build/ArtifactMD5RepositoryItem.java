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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import com.nirima.jenkins.repo.RepositoryDirectory;
import com.nirima.jenkins.repo.fs.VirtualRepositoryItem;

/**
 * Represent a maven repository item.
 */
public class ArtifactMD5RepositoryItem extends VirtualRepositoryItem {

    ArtifactRepositoryItem item = null;
    public ArtifactMD5RepositoryItem(RepositoryDirectory parent, ArtifactRepositoryItem mavenArtifact)
    {
        super(parent, mavenArtifact.getName() + ".md5");
        this.item = mavenArtifact;
    }

    public InputStream getContent() throws Exception {
        return new ByteArrayInputStream(item.getMd5Sum().getBytes("UTF-8"));
    }

    public String getLastModified() {
        return item.getLastModified();
    }

    public Long getSize() {
        return 32L;
    }

    @Override
    protected long getLastModTimeStamp() {
        return 0;
    }
   
}
