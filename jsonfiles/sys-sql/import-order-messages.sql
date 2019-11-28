
IF EXISTS ( SELECT * 
            FROM   sysobjects 
            WHERE  id = object_id(N'[dbo].[z_import_order_message]') 
                   and OBJECTPROPERTY(id, N'IsProcedure') = 1 )
BEGIN
    DROP PROCEDURE z_import_order_message
END
go
create proc z_import_order_message 
  @author_authorName nvarchar(255) = null , 
  @authorEmail  nvarchar(255) = null , 
  @messageDate  datetime = null , 

  @transaction_soID nvarchar(255) = null , 
  @nsid             nvarchar(255) = null,  

  @recipient_recipientName  nvarchar(255) = null , 
  @recipientEmail           nvarchar(255) = null , 

  @cc  nvarchar(255) = null , 
  @bcc nvarchar(255) = null , 

  @subject nvarchar(1024) = null , 
  @message nvarchar(MAX) = null 
as
set nocount on
declare @orderID int = null
set @orderID = (Select top 1 id from tbOrder where NsId = @transaction_soID)
if @transaction_soID is null or  @orderID is null or @nsid is null
begin
	print 'skip message ' + @nsid
	return
end
print 'start import ' + @nsid
declare @orderMemoId int = null
set @orderMemoId = (select id from tbMemo where nsid = @nsid and IDMemoType = 3)
if @orderMemoId is null
begin
	insert into tbMemo(nsid, IDOrder, IDUser, [DateTime]  , IDMemoType, IDLead
					   , IDCase, IDOrderStatus	
					  )
	values            (@nsid, @orderID,    52, @messageDate, 3         , 0 
						, 0    , 10 -- shipped
					  )

	select @orderMemoId = @@IDENTITY
end
update tbMemo 
       set [Description] ='Author: ' + isnull(@author_authorName, 'n/a') +'<br/>'
						  + 'TO: ' + isnull(@recipient_recipientName, '') + '<br/>'
	                      + 'Subject: ' +isnull(@subject, '') +'<br/>'
	                      +'<br/>' + isnull(@message, '')
where id = @orderMemoId

return @orderMemoId

go