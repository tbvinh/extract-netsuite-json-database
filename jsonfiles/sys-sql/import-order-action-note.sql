
IF EXISTS ( SELECT * 
            FROM   sysobjects 
            WHERE  id = object_id(N'[dbo].[z_import_order_message]') 
                   and OBJECTPROPERTY(id, N'IsProcedure') = 1 )
BEGIN
    DROP PROCEDURE z_import_order_action_note
END
go
create proc z_import_order_action_note 
  @title nvarchar(255) = null , 
  @note nvarchar(MAX) = null ,
  @noteDate  datetime = null , 

  @transaction_soID nvarchar(255) = null , 
  @nsid             nvarchar(255) = null,  

  @author_emplId nvarchar(255) = null 
as
set nocount on
declare @orderID int = null
set @orderID = (Select top 1 id from tbOrder where NsId = @transaction_soID)
if @transaction_soID is null or @orderID is null or @nsid is null
begin
	print 'skip action note ' + @nsid
	return
end
print 'start import Action note' + @nsid
declare @orderMemoId int = null
set @orderMemoId = (select id from tbMemo where nsid = @nsid and IDMemoType = 2)
if @orderMemoId is null
begin
	insert into tbMemo(nsid, IDOrder, IDUser, [DateTime]  , IDMemoType, IDLead
					   , IDCase, IDOrderStatus	
					  )
	values            (@nsid, @orderID,    52, @noteDate, 2         , 0 
						, 0    , 10 -- shipped
					  )

	select @orderMemoId = @@IDENTITY
end

declare @puserId int = null

set @puserId = isnull( (Select id From tbUser A where A.nsid = @author_emplId), 52)

update tbMemo 
       set [Description] =
	                      'Title: ' +isnull(@Title, '') +'<br/>'
	                      +'Note:' + isnull(@note, '')
						  , IDUser = @puserId
where id = @orderMemoId

return @orderMemoId

go