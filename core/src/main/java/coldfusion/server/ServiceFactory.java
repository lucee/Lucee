/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package coldfusion.server;

import lucee.runtime.services.DataSourceServiceImpl;

public class ServiceFactory {
	public static final void clear() {

	}

	public static final SecurityService getSecurityService() throws ServiceException {
		throw missingService("SecurityService");
	}

	public static final LoggingService getLoggingService() throws ServiceException {
		throw missingService("LoggingService");
	}

	public static final SchedulerService getSchedulerService() throws ServiceException {
		throw missingService("SchedulerService");
	}

	public static final DataSourceService getDataSourceService() {
		return new DataSourceServiceImpl();
	}

	public static final MailSpoolService getMailSpoolService() throws ServiceException {
		throw missingService("MailSpoolService");
	}

	public static final VerityService getVerityService() throws ServiceException {
		throw missingService("VerityService");
	}

	public static final DebuggingService getDebuggingService() throws ServiceException {
		throw missingService("DebuggingService");
	}

	public static final RuntimeService getRuntimeService() throws ServiceException {
		throw missingService("RuntimeService");
	}

	public static final CronService getCronService() throws ServiceException {
		throw missingService("CronService");
	}

	public static final ClientScopeService getClientScopeService() throws ServiceException {
		throw missingService("ClientScopeService");
	}

	public static final MetricsService getMetricsService() throws ServiceException {
		throw missingService("MetricsService");
	}

	public static final XmlRpcService getXmlRpcService() throws ServiceException {
		throw missingService("XmlRpcService");
	}

	public static final GraphingService getGraphingService() throws ServiceException {
		throw missingService("GraphingService");
	}

	public static final ArchiveDeployService getArchiveDeployService() throws ServiceException {
		throw missingService("ArchiveDeployService");
	}

	public static final RegistryService getRegistryService() throws ServiceException {
		throw missingService("RegistryService");
	}

	public static final LicenseService getLicenseService() throws ServiceException {
		throw missingService("LicenseService");
	}

	public static final DocumentService getDocumentService() throws ServiceException {
		throw missingService("DocumentService");
	}

	public static final EventGatewayService getEventProcessorService() throws ServiceException {
		throw missingService("DocumentService");
	}

	public static final WatchService getWatchService() throws ServiceException {
		throw missingService("WatchService");
	}

	private static ServiceException missingService(String service) {
		// TODO Auto-generated method stub
		return new ServiceException("the service [" + service + "] is currently missing. At the moment you can use cfadmin tag instead");
	}

	public static final void setSecurityService(SecurityService service) {

	}

	public static final void setSchedulerService(SchedulerService service) {

	}

	public static final void setLoggingService(LoggingService service) {

	}

	public static final void setDataSourceService(DataSourceService service) {

	}

	public static final void setMailSpoolService(MailSpoolService service) {

	}

	public static final void setVerityService(VerityService service) {

	}

	public static final void setDebuggingService(DebuggingService service) {

	}

	public static final void setRuntimeService(RuntimeService service) {

	}

	public static final void setCronService(CronService service) {

	}

	public static final void setClientScopeService(ClientScopeService service) {

	}

	public static final void setMetricsService(MetricsService service) {

	}

	public static final void setXmlRpcService(XmlRpcService service) {

	}

	public static final void setGraphingService(GraphingService service) {

	}

	public static final void setArchiveDeployService(ArchiveDeployService service) {

	}

	public static final void setRegistryService(RegistryService service) {

	}

	public static final void setLicenseService(LicenseService service) {

	}

	public static final void setDocumentService(DocumentService service) {

	}

	public static final void setEventProcessorService(EventGatewayService service) {

	}

	public static final void setWatchService(WatchService service) {

	}

}