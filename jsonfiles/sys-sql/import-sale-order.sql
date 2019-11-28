IF EXISTS ( SELECT * 
            FROM   sysobjects 
            WHERE  id = object_id(N'[dbo].[z_import_sale_order]') 
                   and OBJECTPROPERTY(id, N'IsProcedure') = 1 )
BEGIN
    DROP PROCEDURE z_import_sale_order;
END
go
create proc z_import_sale_order
  @autoNumberId int = 405000,
  @createdDate datetime = null,
  @modifiedDate datetime = null,
  @ProcessedDate datetime = null,

  @entity_internalId nvarchar(20) = null, 
  @entity_name nvarchar(255) = null, 

  @tranDate datetime = null, 
  @IDOrderFromOtherDomain nvarchar(255)=null, 
  
  @salesRep_internalId nvarchar(20) = null , 
  @salesRep_name nvarchar(255)=null, 
  @salesTeamList_internalId1 nvarchar(255)=null, 
  @salesTeamList_internalId2 nvarchar(255)=null, 

  @salesTeamList_percentage1 money = 0,
  @salesTeamList_percentage2 money = 0,
  @caseNo nvarchar(255) = null,
  @partner_id  int = null,

  @email nvarchar(50) = null, 
  @billingAddress_internalId nvarchar(20) = null, 
  @billingAddress_country nvarchar(255) = null, 
  @billingAddress_addr1 nvarchar(255) = null, 
  @billingAddress_addr2 nvarchar(255) = null, 
  @billingAddress_addr3 nvarchar(255) = null, 
  @billingAddress_city nvarchar(255) = null, 
  @billingAddress_state nvarchar(255) = null, 
  @billingAddress_zip nvarchar(255) = null, 
  @billingAddress_addrText nvarchar(255) = null, 

  @shippingAddress_internalId nvarchar(20) = null,
  @shippingAddress_country nvarchar(255) = null, 
  @shippingAddress_addr1 nvarchar(255) = null, 
  @shippingAddress_addr2 nvarchar(255) = null, 
  @shippingAddress_addr3 nvarchar(255) = null, 
  @shippingAddress_city nvarchar(255) = null, 
  @shippingAddress_state nvarchar(255) = null, 
  @shippingAddress_zip nvarchar(255) = null, 
  @shippingAddress_addrText nvarchar(255) = null, 
  @shipMethod_internalId int = 0, 

  @shipMethod_name nvarchar(255) = null, 
  @location_internalId nvarchar(20) = null, 
  @location_name varchar(100) = null,
  @subTotal money = 0,
  @taxTotal money = 0,
  @OrderTotal money = 0,
  @status nvarchar(255) = null, 
  @nsid nvarchar(20) = null
as
-- DECLARE

declare @idOwner int = 52 -- System
	, @IDSecondOwner int = 0
	, @idOwner2 int = 0
	, @IDCustomer int = 20 -- lexor@netsuite.com
	, @IDOrderSource int = 4
	, @pid int
-- BEGIN
-- import _pendingFulfillment only
	if @status is null OR @status not like '%BILLED%' 
	begin
		print 'Order not BILLED ' + @IDOrderFromOtherDomain
		return 0
	end
	Print 'start import Order [' + @IDOrderFromOtherDomain +']'

	select @idOwner = id from tbUser where nsid = @salesRep_internalId
	select @idOwner2 = id from tbUser where nsid = @salesTeamList_internalId2
	if @partner_id is not null
	begin
	 set @IDOrderSource = 1 --Lexor
	end
	if @caseNo is not null 
	begin
		set @IDOrderSource = 5 -- Customer Care 
	end
-- default date if null
	if @createdDate is null 
	begin
		set @createdDate = getdate()
	end
	if @modifiedDate is null 
	begin
		set @modifiedDate = getdate()
	end
-- get old value if run many times
	select @pid = id from tbOrder where nsid = @nsid
	if(@pid is null)
	begin
		SET IDENTITY_INSERT tbOrder ON;
		insert into tbOrder(id, nsid) values (@autoNumberId, @nsid)
		SET IDENTITY_INSERT tbOrder OFF;
		select @pid = @autoNumberId -- @@IDENTITY
	end
	if not exists(select 1 from z_import_result where SONumber = @IDOrderFromOtherDomain )
	begin
		Insert into z_import_result(SONumber, OrderId, Note)
		values(@IDOrderFromOtherDomain, @pid, @caseNo);
	end
	else
	begin
		update z_import_result 
			set OrderId = @pid, note = @caseNo
		where SONumber = @IDOrderFromOtherDomain
	end

-- get customer	ID
	select @IDCustomer = id from tbCustomer where nsid = @entity_internalId
	if @ProcessedDate is null
		set @ProcessedDate = '1900-01-01 00:00:00'

 if @pid is null
	return 0
 else
    begin
		--Update Shipping address
		Declare @IDShippingAddress int = null
		
		select @IDShippingAddress = IDShippingAddress From tbOrder_ShippingAddress
		where IDOrder = @pid

		if @IDShippingAddress is null
		begin
			insert into tbAddress(Company, StreetAddress, StreetAddressSelected)
			values(@entity_name, @shippingAddress_addr1, @shippingAddress_addrText)
			select @IDShippingAddress = @@IDENTITY
			insert tbOrder_ShippingAddress(IDOrder, IDShippingAddress, IsBlocked)
			values(@pid, @IDShippingAddress, 0)
		end 
		else
		begin
			update tbAddress
				set StreetAddressSelected = @shippingAddress_addrText
					, StreetAddress = @shippingAddress_addr1
					, Company = @entity_name
			where id = @IDShippingAddress
		end

		-- update Orders
		update tbOrder
		set 
			Subtotal				= @Subtotal, 
			OrderTotal				= @OrderTotal, 
			TaxTotal				= @taxTotal, 
	
			ModifiedDate			= @modifiedDate, 
			ModifiedBy              = @idOwner,
			CreatedDate				= @createdDate,
			ProcessedDate		    = @ProcessedDate,

			IDOrderFromOtherDomain  = @IDOrderFromOtherDomain,
			IDCustomer				= @IDCustomer,
			IDOwner                 = @idOwner,
			IDSecondOwner           = @idOwner2,

			PercentageSecondOwner   = @salesTeamList_percentage2,

			IDOrderType             = 3, --//3	Shopping Cart
			IDOrderStatus           = 10, -- SHIPPED
			IDOrderSource		    = @IDOrderSource, --Sale Rep
			IDOrderPriority			= 5, --/Normal
			IDConfirmedAvailableStock=1,
			IDCommissionType        = 1,
			IDPaymentTerm           = 1,
			IDPaymentStatus         = 3,

			ApprovedShippingDate    = '1900-01-01 00:00:00'
			,Invisible			    = 0
			,Step                   = 7
		where id = @pid


		declare @IDtbOrderMoreInfo int = null
		set @IDtbOrderMoreInfo = (select A.id From tbOrderMoreInfo A
								Where A.IDOrder = @pid )
		if @IDtbOrderMoreInfo is null
			begin
				insert tbOrderMoreInfo(IDOrder
							 , StreetAddressBill, StreetAddressSelectedBill
							 )
				values                (@pid 
				             , isnull( @billingAddress_addrText, '')
							 , isnull(@billingAddress_addr1, '') +' ' 
							  + isnull (@billingAddress_addr2, '')
							)
				select @IDtbOrderMoreInfo = @@IDENTITY
			end
			update tbOrderMoreInfo
				set StreetAddressBill = isnull( @billingAddress_addrText, '')
				, StreetAddressSelectedBill = isnull(@billingAddress_addr1, '') +' ' 
							  + isnull (@billingAddress_addr2, '')
			where id = @IDtbOrderMoreInfo 
        
		return @pid
	end
go