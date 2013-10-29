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

import hudson.maven.MavenBuild;
import hudson.tasks.test.MetaTabulatedResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.nirima.jenkins.repo.RepositoryDirectory;
import com.nirima.jenkins.repo.fs.VirtualRepositoryItem;

/**
 * Represent a maven repository item.
 */
public class MetadataRepositoryItem extends VirtualRepositoryItem {
    
    private static final String MD_FILE_NAME = "maven-metadata.xml";
    byte[] content = null;
    private MavenBuild build = null;
    private List<ArtifactRepositoryItem> items = null;
    
    public MetadataRepositoryItem(RepositoryDirectory parent, MavenBuild build) {
        super(parent, MD_FILE_NAME);
        this.build = build;
        this.items = new ArrayList<ArtifactRepositoryItem>();
    }

    public InputStream getContent() throws Exception {
        return new ByteArrayInputStream(getByteContent());
    }

    private byte[] getByteContent() throws Exception {
        if (content == null) {
            content = createXML();
        }
        return content;
    }

    private byte[] createXML() throws Exception {
        Metadata allMD = null;
        for (ArtifactRepositoryItem item : items) {
            if (allMD == null) {
                allMD = item.getMavenMetadata().getMetadata();
            } else {
                List<SnapshotVersion> currSnaps = allMD.getVersioning().getSnapshotVersions();
                currSnaps.addAll(item.getMavenMetadata().getMetadata().getVersioning().getSnapshotVersions());
            }
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        MetadataXpp3Writer writer = new MetadataXpp3Writer();
        writer.write(byteStream, allMD);
        return byteStream.toByteArray();
    }

    public void addArtifact(ArtifactRepositoryItem item) {
        if (!items.contains(item)) {
            items.add(item);
        }
        content = null;
    }

    protected long getLastModTimeStamp() {
        return build.getTimeInMillis()+build.getDuration();
    }

    public Long getSize() {
        long res = 0; 
        try {
            res = (long) getByteContent().length;
        } catch (Exception e) {
            throw new RuntimeException("failed to get size ",e);
        }
        return res;
    }

    @Override
    public String getDescription() {
        return "Generated file for maven artifact resolving";
    }
}
