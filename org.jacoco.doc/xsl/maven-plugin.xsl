<?xml version="1.0"?>

<!--
   Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
   This program and the accompanying materials are made available under
   the terms of the Eclipse Public License 2.0 which is available at
   http://www.eclipse.org/legal/epl-2.0

   SPDX-License-Identifier: EPL-2.0

   Contributors:
      Evgeny Mandrikov - initial API and implementation
-->

<xsl:stylesheet version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns="http://www.w3.org/1999/xhtml">

  <xsl:param name="outdir" />
  <xsl:param name="qualified.bundle.version" />
  <xsl:param name="jacoco.home.url" />
  <xsl:param name="copyright.years" />

  <xsl:template match="/">
    <xsl:for-each select="plugin/mojos/mojo">
      <xsl:message terminate="no">
        <xsl:value-of select="$outdir"/>
      </xsl:message>
      <xsl:result-document href="file:///{$outdir}/{goal}-mojo.html" method="xml" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    <html>
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="resources/doc.css" charset="UTF-8"
    type="text/css" />
    <link rel="shortcut icon" href="resources/report.gif" type="image/gif" />
    <title>
    JaCoCo -
    <xsl:value-of select="../../goalPrefix"/>:<xsl:value-of select="goal" />
    </title>
    </head>
          <body>
           <div class="breadcrumb">
           <a href="../index.html" class="el_report">JaCoCo</a>
           &gt;
           <a href="index.html" class="el_group">Documentation</a>
           &gt;
           <a href="maven.html" class="el_group">Maven</a>
           &gt;
           <span class="el_source">
           <xsl:value-of select="../../goalPrefix"/>:<xsl:value-of select="goal" />
           </span>
           </div>
           <div id="content">
            <h1>jacoco:<xsl:value-of select="goal"/></h1>

            <p><b>Full name</b> :</p>
            <xsl:value-of select="../../groupId"/>:<xsl:value-of select="../../artifactId"/>:<xsl:value-of select="../../version"/>:<xsl:value-of select="goal" />

            <p><b>Description</b> :</p>
            <div><xsl:value-of select="description" disable-output-escaping="yes" /></div>

            <p><b>Attributes</b> :</p>
            <ul>
              <xsl:if test="requiresProject='true'">
              <li>Requires a Maven project to be executed.</li>
              </xsl:if>
              <xsl:if test="requiresDependencyResolution">
              <li>Requires dependency resolution of artifacts in scope: <code><xsl:value-of select="requiresDependencyResolution"/></code> .</li>
              </xsl:if>
              <li>Since version: <code><xsl:value-of select="since"/></code> .</li>
              <li>Binds by default to the <a href="http://maven.apache.org/ref/current/maven-core/lifecycles.html">lifecycle phase</a> : <code><xsl:value-of select="phase"/></code> .</li>
            </ul>

            <xsl:if test="count(parameters/parameter[required='true' and editable='true']) != 0">
            <h2>Required Parameters</h2>
            <table class="coverage">
              <thead>
              <tr>
                <td>Name</td>
                <td>Type</td>
                <td>Since</td>
                <td>Description</td>
              </tr>
              </thead>
              <xsl:for-each select="parameters/parameter[required='true' and editable='true']">
                <xsl:call-template name="parameter"/>
              </xsl:for-each>
            </table>
            </xsl:if>

            <xsl:if test="count(parameters/parameter[required='false' and editable='true']) != 0">
            <h2>Optional Parameters</h2>
            <table class="coverage">
              <thead>
              <tr>
                <td>Name</td>
                <td>Type</td>
                <td>Since</td>
                <td>Description</td>
              </tr>
              </thead>
              <tbody>
              <xsl:for-each select="parameters/parameter[required='false' and editable='true']">
                <xsl:call-template name="parameter"/>
              </xsl:for-each>
              </tbody>
            </table>
            </xsl:if>

            <h2>Parameter Details</h2>
            <xsl:for-each select="parameters/parameter[editable='true']">
              <b><a name="{name}">&lt;<xsl:value-of select="name"/>&gt;</a></b>
              <div><xsl:value-of select="description" disable-output-escaping="yes" /></div>
              <ul>
                <li><b>Type</b> : <code><xsl:value-of select="type"/></code></li>
                <li>
                  <b>Since</b> :
                  <code>
                    <xsl:choose>
                      <xsl:when test="since"><xsl:value-of select="since"/></xsl:when>
                      <xsl:otherwise><xsl:value-of select="../../since"/></xsl:otherwise>
                    </xsl:choose>
                  </code>
                </li>
                <li>
                  <b>Required</b> :
                  <code>
                    <xsl:choose>
                      <xsl:when test="required='true'">Yes</xsl:when>
                      <xsl:otherwise>No</xsl:otherwise>
                    </xsl:choose>
                  </code>
                </li>
                <xsl:variable name="configuration" select="../../configuration/*[name()=current()/name]"/>
                <xsl:if test="$configuration != ''">
                <li><b>User Property</b> : <code><xsl:value-of select="replace($configuration, '^\$\{(.*)\}$', '$1')"/></code></li>
                </xsl:if>
                <xsl:if test="$configuration/@default-value">
                <li><b>Default</b> : <code><xsl:value-of select="$configuration/@default-value"/></code></li>
                </xsl:if>
              </ul>
            </xsl:for-each>

           </div>
<div class="footer">
<span class="right">
<a href="{$jacoco.home.url}">JaCoCo</a>
&#160;
<xsl:value-of select="$qualified.bundle.version" />
</span>
<a href="../doc/license.html">Copyright</a>
&#169;
<xsl:value-of select="$copyright.years" />
Mountainminds GmbH &amp; Co. KG and Contributors
</div>
          </body>
        </html>
      </xsl:result-document>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="parameter">
    <tr>
      <td>
        <code><a href="#{name}">&lt;<xsl:value-of select="name"/>&gt;</a></code>
      </td>
      <td>
        <!-- TODO truncate - see https://github.com/apache/maven-plugin-tools/blob/master/maven-plugin-report-plugin/src/main/java/org/apache/maven/plugin/plugin/report/GoalRenderer.java#L413 -->
        <code><xsl:value-of select="tokenize(tokenize(type, '[&lt;]')[1], '[.]')[last()]"/></code>
      </td>
      <td>
        <code>
          <xsl:choose>
            <xsl:when test="since"><xsl:value-of select="since"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="../../since"/></xsl:otherwise>
          </xsl:choose>
        </code>
      </td>
      <td>
        <xsl:value-of select="description" disable-output-escaping="yes" />
        <xsl:variable name="configuration" select="../../configuration/*[name()=current()/name]"/>
        <xsl:if test="$configuration/@default-value">
        <br/>
        <b>Default value is</b> : <code><xsl:value-of select="$configuration/@default-value"/></code> .
        </xsl:if>
        <xsl:if test="$configuration != ''">
        <br/>
        <b>User property is</b> : <code><xsl:value-of select="replace($configuration, '^\$\{(.*)\}$', '$1')"/></code> .
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
