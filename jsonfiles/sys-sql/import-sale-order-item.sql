IF EXISTS ( SELECT * 
            FROM   sysobjects 
            WHERE  id = object_id(N'[dbo].[z_import_item]') 
                   and OBJECTPROPERTY(id, N'IsProcedure') = 1 )
BEGIN
    DROP PROCEDURE z_import_item
END
go
create proc z_import_item
	@parentId int
    , @itemList_item_item_internalId int = 0
    , @itemList_item_item_name nvarchar(255) = null
    , @itemList_item_item_color nvarchar(255) = null
	
	, @itemList_item_quantity money = 0
    , @itemList_item_units_name nvarchar(255) = null
	, @itemList_item_price money = 0
	, @itemList_item_rate money = 0
	, @itemList_item_amount money = 0
    , @itemList_item_location_name nvarchar(255) = null
	, @itemList_item_lineUniqueKey nvarchar(20) = null
	, @itemList_item_taxRate1 nvarchar(255) = null
	, @itemList_item_customFieldList_value money = 0
as
	declare @IDColor int, @IDProduct int

	if @parentId = 0 or @parentId is null return  --<<<<<<<<<<<
	print '____start import item ['+ str(@parentId) +'] -> ' +  @itemList_item_item_name
	if not exists(select 1 from tbColor where EnName = @itemList_item_item_color)
	begin
		print 'insert color ' + @itemList_item_item_color

		insert into tbColor(VNName, EnName, Sort,IDColorTitle)
		values(@itemList_item_item_color, @itemList_item_item_color, 1,1)
	end

	if not exists(select 1 from tbProduct where EnName = @itemList_item_item_name)
	begin
		insert into tbProduct(VNName,	 EnName,				   Sort, IDCategory)
		values(@itemList_item_item_name, @itemList_item_item_name, 1   , 1)
	end

	set @IDColor = (select top 1 ID From tbColor A where A.ENName = @itemList_item_item_color)
	set @IDProduct = (select top 1 ID From tbProduct A where A.ENName = @itemList_item_item_name)
	print '@IDColor= ' + str(@IDColor)
	if not exists(select 1 from tbProduct_Color A where A.IDProduct = @IDProduct and A.IDColor = @IDColor)
	begin
		insert into tbProduct_Color(IDProduct, IDColor)
		values                     (@IDProduct,@IDColor)
	end

	declare @pid int 
	select @pid = id from tbOrderDetail where nsid = @itemList_item_lineUniqueKey

	if @itemList_item_customFieldList_value > 0  --/* Reset customimize price */
	begin
	   set @itemList_item_rate = @itemList_item_customFieldList_value
	end

	if @pid is null
	begin
		print 'insert order detail'
		insert into tbOrderDetail(IDOrder,  IDProduct,  IDColor, Quantity,				  Price              , nsid               
		, IDIncluded)
		values					 (@parentId,@IDProduct, @IDColor,@itemList_item_quantity, @itemList_item_rate, @itemList_item_lineUniqueKey
		, 1) 
		select @pid = @@IDENTITY
	end
	else
	begin
		print 'update order detail' + str(@parentId) +' UniqueKey:' +  str(@itemList_item_lineUniqueKey)
		update tbOrderDetail
			set Quantity = @itemList_item_quantity
				,Price   = @itemList_item_rate
				,IDColor = @IDColor
				,IDProduct = @IDProduct
				,IDIncluded = 1
		where id = @pid
	end
go