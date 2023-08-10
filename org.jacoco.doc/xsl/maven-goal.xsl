<?xml version="1.0"?>

<!--
   Copyright (c) 2009, 2023 Mountainminds GmbH & Co. KG and Contributors
   This program and the accompanying materials are made available under
   the terms of the Eclipse Public License 2.0 which is available at
   http://www.eclipse.org/legal/epl-2.0

   SPDX-License-Identifier: EPL-2.0

   Contributors:
      Marc R. Hoffmann - initial API and implementation
      Kyle Lieber - implementation of CheckMojo
-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xdoc="http://maven.apache.org/XDOC/2.0"
	xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="xdoc">

	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" />

	<xsl:param name="qualified.bundle.version" />
	<xsl:param name="jacoco.home.url" />
	<xsl:param name="copyright.years" />

	<xsl:template match="/">
		<html>
			<head>
				<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
				<link rel="stylesheet" href="resources/doc.css" charset="UTF-8"
					type="text/css" />
				<link rel="shortcut icon" href="resources/report.gif" type="image/gif" />
				<title>
					JaCoCo -
					<xsl:value-of select="xdoc:document/xdoc:properties/xdoc:title" />
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
						<xsl:value-of select="xdoc:document/xdoc:properties/xdoc:title" />
					</span>
				</div>
				<div id="content">
					<xsl:apply-templates select="xdoc:document/xdoc:body" />
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
	</xsl:template>

	<xsl:template match="xdoc:section">
		<h1>
			<xsl:value-of select="@name" />
		</h1>
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="xdoc:subsection">
		<h2>
			<xsl:value-of select="@name" />
		</h2>
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="xdoc:p">
		<p>
			<xsl:apply-templates />
		</p>
	</xsl:template>

	<xsl:template match="xdoc:div">
		<div>
			<xsl:apply-templates />
		</div>
	</xsl:template>

	<xsl:template match="xdoc:a[@href]">
		<a>
			<xsl:attribute name="href">
				<xsl:value-of select="@href" />
  			</xsl:attribute>
			<xsl:apply-templates />
		</a>
	</xsl:template>

	<xsl:template match="xdoc:a[@name]">
		<a>
			<xsl:attribute name="name">
				<xsl:value-of select="@name" />
  			</xsl:attribute>
			<xsl:apply-templates />
		</a>
	</xsl:template>

	<xsl:template match="xdoc:strong">
		<b>
			<xsl:apply-templates />
		</b>
	</xsl:template>

	<xsl:template match="xdoc:code">
		<code>
			<xsl:apply-templates />
		</code>
	</xsl:template>

	<xsl:template match="xdoc:pre">
		<pre>
			<xsl:apply-templates />
		</pre>
	</xsl:template>

	<xsl:template match="xdoc:br">
		<br />
	</xsl:template>

	<xsl:template match="xdoc:ul">
		<ul>
			<xsl:apply-templates />
		</ul>
	</xsl:template>

	<xsl:template match="xdoc:li">
		<li>
			<xsl:apply-templates />
		</li>
	</xsl:template>

	<xsl:template match="xdoc:table">
		<table class="coverage">
			<thead>
				<tr>
					<xsl:for-each select="xdoc:tr/xdoc:th">
						<td>
							<xsl:apply-templates />
						</td>
					</xsl:for-each>
				</tr>
			</thead>
			<tbody>
				<xsl:for-each select="xdoc:tr[xdoc:td]">
					<tr>
						<xsl:for-each select="xdoc:td">
							<td>
								<xsl:apply-templates />
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:template>

</xsl:stylesheet>
