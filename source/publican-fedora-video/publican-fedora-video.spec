%define brand fedora-video
%define RHEL6 %(test "%{?dist}" == ".el6" && echo 1 || echo 0)

Name:		publican-%{brand}
Summary:	Publican documentation template files for %{brand}
Version: 2.0
Release:	4%{?dist}
License:	CC-BY-SA
Group:		Development/Libraries
Buildroot:	%{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
# Limited to these arches on RHEL 6 due to PDF + Java limitations
%if %{RHEL6}
ExclusiveArch:   i686 x86_64
%else
BuildArch:   noarch
%endif
Source:		https://fedorahosted.org/releases/publican/%{name}-%{version}.tgz
Requires:	publican >= 2.0
BuildRequires:	publican >= 2.0
URL:		https://publican.fedorahosted.org
Obsoletes:	documentation-devel-Fedora

%description
This package provides common files and templates needed to build documentation
for %{brand} with publican.

%prep
%setup -q

%build
publican build --formats=xml --langs=all --publish

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p -m755 $RPM_BUILD_ROOT%{_datadir}/publican/Common_Content
publican install_brand --path=$RPM_BUILD_ROOT%{_datadir}/publican/Common_Content

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
%doc README
%doc COPYING
%{_datadir}/publican/Common_Content/%{brand}

%changelog

* Thu Jul 05 2012 Joshua Wulf <jwulf@fedoraproject.org> 2.0-4
- Added section.autolabel.max.depth to stop sections at level 2
- Changed css styling to remove bold from a number of monospaced elements, such as code and literal

* Tue Jul 03 2012 Joshua Wulf <jwulf@fedoraproject.org> 2.0-3
- Added "simpletarget" xrefstyle to support target title only (no structure metadata) in HTML-rendered xrefs

* Thu May 17 2012 Joshua Wulf <jwulf@fedoraproject.org> 2.0-2
- Added custom css for styling Docs Hack clickable links
- Added custom css to enable integration in the Docs Hack Server

* Thu May 17 2012 Joshua Wulf <jwulf@fedoraproject.org> 2.0-1
- Add support for video display in Firefox

* Sun Aug 29 2010 Rüdiger Landmann <r.landmann@redhat.com> 2.0-0
- Extend callout graphics to 40; adjust colour and font BZ #629804 <r.landmann@redhat.com>
- Restrict CSS style for edition to title pages to avoid applying to bibliographies <r.landmann@redhat.com>

* Sun Aug 29 2010 Rüdiger Landmann <r.landmann@redhat.com> 1.9-0
- Note ownership of MySQL trademark per Pamela Chestek <pchestek@redhat.com>
- Low German translation from Nils-Christoph Fiedler <ncfiedler@fedoraproject.org>
- Persian translation from Mostafa Daneshvar <info@mostafadaneshvar.com>

* Mon Jul 12 2010 Rüdiger Landmann <r.landmann@redhat.com> 1.8-0
- Localizations for image_right.png

* Mon Jul 5 2010 Jeff Fearn <jfearn@redhat.com> 1.7
- Port to Publican 2

* Thu Jun 10 2010 Jeff Fearn <jfearn@redhat.com> 1.6
- Remove HTML term color. BZ #592822

* Tue May 04 2010 Rüdiger Landmann <r.landmann@redhat.com> 1.5-0
- turn article ToCs back on for now

* Thu Mar 25 2010 Rüdiger Landmann <r.landmann@redhat.com> 1.4-0
- add trademark notices for XFS and Java per Pamela Chestek <pchestek@redhat.com>

* Fri Nov 20 2009 Rüdiger Landmann <rlandmann@redhat.com> 1.3-0
- rename sr-LATN to sr-Latn-RS

* Fri Nov 20 2009 Ryan Lerch <rlerch@redhat.com> 1.2-0
- new Documentation graphic -- image_right.png

* Mon Nov 9 2009 Rüdiger Landmann <rlandmann@redhat.com> 1.1
- update license to CC-BY-SA (BZ#533741)

* Fri Oct 30 2009 Jeff Fearn <jfearn@redhat.com> 1.0
- port to publican 1.0.

* Fri Mar 13 2009  Jeff Fearn <jfearn@redhat.com> 0.19
- Fix right to left fo ar-AR. BZ #486162
- Patches and translations by Muayyad Alsadi <alsadi@ojuba.org> 

* Mon Mar 9 2009  Jeff Fearn <jfearn@redhat.com> 0.18
- Add symlinks for langauges without country codes. BZ #487256
- Fri Mar 6 2009 Piotr Drąg <piotrdrag@gmail.com>
- Translate Fedora Feedback into Polish
- Wed Mar 4 2009 Miloš Komarčević <kmilos@gmail.com>
- Translate Fedora Feedback into Serbian
- Translate Fedora Feedback into Serbian (Latin)
- Wed Mar 4 2009 Richard van der Luit <zuma@xs4all.nl>
- Corrections to Fedora Feedback in Dutch
- Sat Feb 28 2009 Rui Gouveia <rui.gouveia@globaltek.pt>
- Translate Fedora Feedback into European Portuguese
- Fri Feb 27 2009 Richard van der Luit <zuma@xs4all.nl>
- Translate Fedora Feedback into Dutch

* Mon Jan 5 2009 Jeff Fearn <jfearn@redhat.com> 0.17
- Add LICENSE override for RPMs. BZ #477720

* Mon Dec 1 2008 Jeff Fearn <jfearn@redhat.com> 0.16
- resave title_logo.png. BZ #474075
- Add override for PROD_URL

* Tue Sep 9 2008 Jeff Fearn <jfearn@redhat.com> 0.15
- Removed corpauthor from template. BZ #461222
- Updated Fedora legal notice. BZ #448022

* Mon Sep 1 2008 Jeff Fearn <jfearn@redhat.com> 0.14-0
- Fix styles for publican 0.35 mods
- Removed common entity files as they break translation
- Remove ID's from common files. BZ #460770

* Mon Apr 14 2008 Jeff Fearn <jfearn@redhat.com> 0.13-0
- Fix missing list image in html-single articles
- QANDA set css fix BZ #442674
- Override PDF Theme
- Added package tag BZ #444908
- Added Article and Set Templates

* Mon Apr 7 2008 Jeff Fearn <jfearn@redhat.com> 0.12-0
- Add Desktop css customisations

* Tue Mar 4 2008 Andy Fitzsimon <afitzsim@redhat.com> 0.11-0
- optimised default stylesheet colours
- author group improvements
- formatting for revision history 
- merged tocnav and documentation.css to defauly.css
- updated icons

* Thu Feb 28 2008 Jeff Fearn <jfearn@redhat.com> 0.10-0
- Added PRODUCT entity with default msg. BZ #431171
- Added BOOKID entity with default msg. BZ #431171
- Fix keycap hard to read in admon BZ #369161
- Added Brand Makefile
- Fix docs URL BZ #434733

* Thu Feb 14 2008 Jeff Fearn <jfearn@redhat.com> 0.9-0
- Changed Group
- Updated Legal Notice
- Removed Requires(post) and Requires(postun)
- Added missing logo.png
- Updated Summary

* Wed Feb 13 2008 Jeff Fearn <jfearn@redhat.com> 0.8-1
- Correct License name
- Remove release from urls and file names

* Tue Feb 12 2008 Jeff Fearn <jfearn@redhat.com> 0.8-0
- Setup per Brand Book_Templates
- Fix soure and URL paths
- Use release in source path
- add OPL text as COPYING

* Mon Feb 11 2008 Jeff Fearn <jfearn@redhat.com> 0.7-0
- Updated YEAR entity with better message.

* Fri Feb 01 2008 Jeff Fearn <jfearn@redhat.com> 0.6-0
- Switch from documentation-devil to publican

* Thu Jan 17 2008 Jeff Fearn <jfearn@redhat.com> 0.5-0
- Switch from documentation-devel to documentation-devil

* Wed Jan 02 2008 Jeff Fearn <jfearn@redhat.com> 0.4-0
- Default YEAR and HOLDER. BZ #426040

* Fri Jul 20 2007 Jeff Fearn <jfearn@redhat.com> 0.3-0
- Update

* Fri Jul 13 2007 Jeff Fearn <jfearn@redhat.com> 0.1-0
- Initial creation
