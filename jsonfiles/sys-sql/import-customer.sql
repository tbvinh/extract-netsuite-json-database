IF EXISTS ( SELECT * 
            FROM   sysobjects 
            WHERE  id = object_id(N'[dbo].[z_import_customer]') 
                   and OBJECTPROPERTY(id, N'IsProcedure') = 1 )
BEGIN
    DROP PROCEDURE z_import_customer
END
go
create proc z_import_customer 
    @companyName nvarchar(255) = '',
	@firstName nvarchar(255) = '', 
	@lastname nvarchar(255) = '', @BusinessPhone nvarchar(20) = null, @email nvarchar(255)=null, 
	@StreetAddress nvarchar(255) ='',

	@BillAddress1 nvarchar(255) ='', @BillAddress2 nvarchar(255) ='', 
	@BillAddress3 nvarchar(255) ='', @BillCity nvarchar(255) = '', @BillState nvarchar(255) = '', @BillZip nvarchar(255) = '',
	@Bill_nsid    int = null, 

	@ShippingAddress1 nvarchar(255) ='', @ShippingAddress2 nvarchar(255) ='', 
	@ShippingAddress3 nvarchar(255) ='', @ShippingCity nvarchar(255) = '', @ShippingState nvarchar(255) = '', @ShippingZip nvarchar(255) = '',
	@Shipping_nsid    int = null,
	@nsid varchar(20) = null
as

set nocount on
	
	if @StreetAddress is null or len(@StreetAddress) = 0 return --////////////

	declare @NameDisplay nvarchar(255)
	declare @pid int = null, @pAddressCustId int = null
	set @NameDisplay = @firstName +'@' + @lastname

	if(@email is null) set @email = @NameDisplay 
	
	set @BusinessPhone = replace(@BusinessPhone, ' ', '')
	set @BusinessPhone = replace(@BusinessPhone, '-', '')
	set @BusinessPhone = replace(@BusinessPhone, '(', '')
	set @BusinessPhone = replace(@BusinessPhone, ')', '')
	
	

	select @pAddressCustId = IDCustomer from tbAddress where tbAddress.StreetAddressSelected = @StreetAddress
	if @pAddressCustId is not null 
		begin
			update tbCustomer set nsid = @nsid 
			, IDCustomerType = 1, Company = @companyName
			where id = @pAddressCustId
		end
	else
	begin
		select @pid = id from tbCustomer where FirstName = @firstName and LastName = @lastname and Email = @email
		if @pid is null
		begin
			insert into tbCustomer(nsid, FirstName, lastName, Email, IDCustomerType, Company) 
			values               (@nsid, @firstName, @lastname, @email, 1,           @companyName)
			select @pid = @@IDENTITY
		end 
		else
		begin
			insert tbAddress(IDCustomer, StreetAddress, StreetAddressSelected, BusinessPhone)
				values      (@pid      , @StreetAddress,@StreetAddress       , @BusinessPhone)
		end
		
	end
go
/*
update tbCustomer set IDCustomerType = 1 where IDCustomerType is null
*/