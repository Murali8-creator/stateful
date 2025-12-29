# python script
# import pika
# import time
# from tenacity import retry, stop_after_attempt, wait_fixed
#
#
# # This is the "Self-Healing" logic
# # It says: "If this function fails, try again up to 3 times, waiting 2 seconds between each."
# @retry(stop=stop_after_attempt(3), wait=wait_fixed(2))
# def heavy_ml_task(body):
#     print(f" [Python] Attempting ML task for: {body.decode()}")
#
#     # Simulate a temporary failure (like a 1-second network glitch)
#     # On the first two tries, we will fail. On the 3rd, we will succeed!
#     if not hasattr(heavy_ml_task, "count"):
#         heavy_ml_task.count = 0
#
#     heavy_ml_task.count += 1
#
#     if heavy_ml_task.count <= 3:
#         print(" [Python] ERROR: Temporary glitch... retrying.")
#         raise RuntimeError("Temporary Connection Issue")
#
#     print(" [Python] SUCCESS! Task completed on attempt #3.")
#
#
# def main():
#     connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
#     channel = connection.channel()
#
#     # Define the arguments to MATCH your Java RabbitMqConfig
#     # 'app.dlq.exchange' must match DLQ_EXCHANGE in Java
#     # 'ml.dead' must match the key used in Java
#     args = {
#         'x-dead-letter-exchange': 'app.dlq.exchange',
#         'x-dead-letter-routing-key': 'ml.dead'
#     }
#
#     # Declare the queue with the matching arguments
#     channel.queue_declare(queue='ml.processing.queue', durable=True, arguments=args)
#
#     def callback(ch, method, properties, body):
#         try:
#             heavy_ml_task(body)
#             ch.basic_ack(delivery_tag=method.delivery_tag)
#         except Exception as e:
#             print(f" [Python] Permanent failure after all retries: {e}")
#             # This sends it to the DLQ because requeue=False
#             ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)
#
#     channel.basic_consume(queue='ml.processing.queue', on_message_callback=callback)
#     print(' [*] Python ML Service is running with RETRIES. Waiting for tasks...')
#     channel.start_consuming()
#
#
# if __name__ == '__main__':
#     main()
